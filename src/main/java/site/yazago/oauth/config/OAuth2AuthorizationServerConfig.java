package site.yazago.oauth.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableAuthorizationServer
//@Import({AuthorizationServerTokenServicesConfiguration.class})
@EnableConfigurationProperties(AuthorizationServerProperties.class)
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;

    private final DataSource dataSource;

    private final TokenStore tokenStore;
    private final UserDetailsService userDetailsService;


    private final JwtAccessTokenConverter accessTokenConverter;

    public OAuth2AuthorizationServerConfig(@Qualifier("authenticationManagerBean") AuthenticationManager authenticationManager,
                                           DataSource dataSource,
                                           TokenStore tokenStore,
                                           UserDetailsService userDetailsService,
                                           JwtAccessTokenConverter accessTokenConverter) {
        this.authenticationManager = authenticationManager;
        this.dataSource = dataSource;
        this.tokenStore = tokenStore;
        this.userDetailsService = userDetailsService;
        this.accessTokenConverter = accessTokenConverter;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .userDetailsService(userDetailsService)
                .tokenStore(tokenStore)
                .tokenEnhancer(accessTokenConverter)
                .accessTokenConverter(accessTokenConverter)
                .authenticationManager(authenticationManager);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/api/register/**").permitAll()
                    .anyRequest().authenticated();
        }
    }

//    Все что ниже копипаста с небольшими изменениями из org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerTokenServicesConfiguration

    @Configuration
    @Conditional(JwtKeyStoreCondition.class)
    protected static class JwtKeyStoreConfiguration implements ApplicationContextAware {

        private final AuthorizationServerProperties authorization;
        private ApplicationContext context;

        @Autowired
        public JwtKeyStoreConfiguration(AuthorizationServerProperties authorization) {
            this.authorization = authorization;
        }

        @Override
        public void setApplicationContext(ApplicationContext context) throws BeansException {
            this.context = context;
        }

        @Bean
        @ConditionalOnMissingBean(AuthorizationServerTokenServices.class)
        public DefaultTokenServices jwtTokenServices(TokenStore jwtTokenStore) {
            DefaultTokenServices services = new DefaultTokenServices();
            services.setTokenStore(jwtTokenStore);
            return services;
        }

        @Bean
        @ConditionalOnMissingBean(TokenStore.class)
        public TokenStore tokenStore() {
            return new JwtTokenStore(accessTokenConverter());
        }

        @Bean
        public JwtAccessTokenConverter accessTokenConverter() {
            Assert.notNull(authorization.getJwt().getKeyStore(), "keyStore cannot be null");
            Assert.notNull(authorization.getJwt().getKeyStorePassword(), "keyStorePassword cannot be null");
            Assert.notNull(authorization.getJwt().getKeyAlias(), "keyAlias cannot be null");

            JwtAccessTokenConverter converter = new CustomJwtAccessTokenConverter();

            Resource keyStore = context.getResource(authorization.getJwt().getKeyStore());
            char[] keyStorePassword = authorization.getJwt().getKeyStorePassword().toCharArray();
            KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(keyStore, keyStorePassword);

            String keyAlias = authorization.getJwt().getKeyAlias();
            char[] keyPassword = Optional.ofNullable(
                    authorization.getJwt().getKeyPassword())
                    .map(String::toCharArray).orElse(keyStorePassword);
            converter.setKeyPair(keyStoreKeyFactory.getKeyPair(keyAlias, keyPassword));

            return converter;
        }
    }


    private static class JwtKeyStoreCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context,
                                                AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage
                    .forCondition("OAuth JWT KeyStore Condition");
            Environment environment = context.getEnvironment();
            String keyStore = environment
                    .getProperty("security.oauth2.authorization.jwt.key-store");
            if (StringUtils.hasText(keyStore)) {
                return ConditionOutcome
                        .match(message.foundExactly("provided key store location"));
            }
            return ConditionOutcome
                    .noMatch(message.didNotFind("provided key store location").atAll());
        }

    }

}
