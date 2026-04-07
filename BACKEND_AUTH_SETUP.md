# Configuración del Backend - Spring Security con Auth0

## Verificar Configuración en application.properties

Tu backend ya tiene configuradas las propiedades de Auth0:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dev-robgo.us.auth0.com/
spring.security.oauth2.resourceserver.jwt.audiences=https://api-demo-auth0/
```

## Verificar SecurityConfig.java

Asegúrate de que tu `SecurityConfig.java` esté configurado correctamente:

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/**").permitAll()  // Permitir health check
                .anyRequest().authenticated()                 // Todas las demás rutas requieren auth
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("permissions");
        converter.setAuthorityPrefix("SCOPE_");
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",      // Frontend desarrollo
            "http://localhost:3000",      // Alternativa
            "http://localhost:8080"       // Si el frontend está en el mismo servidor
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

## Agregar @CrossOrigin a los Controllers (Alternativa a CORS Global)

Si prefieres configurar CORS por controller:

```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
public class PacienteController {
    // ... tu código
}
```

## Dependencias Maven Requeridas

Asegúrate de tener estas dependencias en `pom.xml`:

```xml
<!-- Spring Security OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

## Testing de la Autenticación

### 1. Obtener un token de Auth0

**Opción A: Desde el Frontend (Recomendado)**
```typescript
// En consola del navegador
const token = await window.authStore.getTokenSilently()
console.log(token)
```

**Opción B: Desde el CLI (para testing)**
```bash
curl --request POST \
  --url https://dev-robgo.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{"client_id":"swvOloLZ3XnxkJMv9XhoShG4XH9k8Hm7","client_secret":"YOUR_CLIENT_SECRET","audience":"https://api-demo-auth0/","grant_type":"client_credentials"}'
```

### 2. Usar el token para hacer una request

```bash
curl --request GET \
  --url http://localhost:8080/api/pacientes \
  --header 'Authorization: Bearer YOUR_TOKEN' \
  --header 'Content-Type: application/json'
```

### 3. Decodificar el JWT
Ir a [https://jwt.io/](https://jwt.io/) y pegar el token para ver su contenido.

**El payload debería verse así:**
```json
{
  "iss": "https://dev-robgo.us.auth0.com/",
  "sub": "auth0|...",
  "aud": "https://api-demo-auth0/",
  "iat": 1712345678,
  "exp": 1712432078,
  "scope": "openid profile email"
}
```

## Solución de Problemas

### Error: "Invalid issuer"
**Causa:** El issuer en el token no coincide con `spring.security.oauth2.resourceserver.jwt.issuer-uri`

**Solución:**
- Verificar que Auth0 domain sea exacto en properties
- El token que recibiste es de un Auth0 domain diferente

### Error: "Invalid audience"
**Causa:** El audience en el token no coincide con los configurados

**Solución:**
- Verificar que el `VITE_AUTH0_AUDIENCE` en el frontend sea exactamente igual al configurado en `application.properties`
- En el authorization params del login, asegúrate de pasar el audience

### Error: 401 Unauthorized
**Causas:**
- No se está enviando el Authorization header
- El token está expirado
- El token es inválido
- CORS pre-flight está fallando

**Solución:**
- Verificar que el token se esté enviando
- Revisar logs del backend
- Verificar CORS está habilitado

### Error: CORS (No 'Access-Control-Allow-Origin' header)
**Causa:** Backend no tiene CORS habilitado

**Solución:**
- Verificar que `CorsConfigurationSource` está registrado
- O agregar `@CrossOrigin` a los controllers
- Verificar que el origen del frontend está en la lista blanca

## Estructura de Seguridad

```
┌─────────────────────────────┐
│   Frontend (Vue.js)         │
│   - Obtiene token de Auth0  │
│   - Envía token en header   │
└────────────┬────────────────┘
             │ Authorization: Bearer <token>
             ▼
┌─────────────────────────────┐
│   Spring Security           │
│   - CORS Filter             │
│   - OAuth2 Resource Server  │
│   - JWT Validator           │
├─────────────────────────────┤
│ Validaciones:               │
│ 1. Verificar firma del JWT  │
│ 2. Verificar issuer         │
│ 3. Verificar audience       │
│ 4. Verificar expiration     │
└────────────┬────────────────┘
             │ ✓ Token válido
             ▼
┌─────────────────────────────┐
│   Controller (@RestController)
│   - PacienteController      │
│   - DoctorController        │
│   - etc.                    │
└─────────────────────────────┘
```

## Monitoreo

### Ver logs de Spring Security

En `application.properties`:
```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2=DEBUG
```

### Endpoints de Health Check

El actuator expone:
```
GET http://localhost:8080/actuator/health
GET http://localhost:8080/actuator/health/db
GET http://localhost:8080/actuator/health/diskSpace
```

## Próximas Mejoras

- [ ] Implementar roles y permisos (RBAC)
- [ ] Agregar @PreAuthorize en métodos de controller
- [ ] Implementar method-level security
- [ ] Agregar audit logging
- [ ] Rate limiting
- [ ] Token rotation

## Referencias

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Auth0 with Spring Boot](https://auth0.com/docs/quickstart/backend/java-spring-boot)
- [OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWT with Spring](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
