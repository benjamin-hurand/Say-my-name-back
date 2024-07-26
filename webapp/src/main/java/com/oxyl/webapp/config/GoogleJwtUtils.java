//package com.oxyl.webapp.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayInputStream;
//import java.net.URL;
//import java.security.PublicKey;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class GoogleJwtUtils {
//
//    public Claims getAllClaimsFromGoogleJwtToken(String token) {
//        try {
//            return Jwts.parser()
//                    .setSigningKeyResolver(new GoogleSigningKeyResolver())
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (Exception e) {
//            throw new RuntimeException("Invalid Google token", e);
//        }
//    }
//
//    private static class GoogleSigningKeyResolver implements SigningKeyResolver {
//
//        private static final String GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";
//        private Map<String, PublicKey> publicKeys;
//
//        public GoogleSigningKeyResolver() {
//            this.publicKeys = fetchGooglePublicKeys();
//        }
//
//        @Override
//        public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
//            PublicKey publicKey = publicKeys.get(header.getKeyId());
//            return publicKey != null ? publicKey.getEncoded() : null;
//        }
//
//        private Map<String, PublicKey> fetchGooglePublicKeys() {
//            Map<String, PublicKey> keys = new HashMap<>();
//            try {
//                URL url = new URL(GOOGLE_CERTS_URL);
//                ObjectMapper mapper = new ObjectMapper();
//                Map<String, Object> certs = mapper.readValue(url, Map.class);
//
//                List<Map<String, String>> keysList = (List<Map<String, String>>) certs.get("keys");
//                for (Map<String, String> keyData : keysList) {
//                    String kid = keyData.get("kid");
//                    String x5c = keyData.get("x5c").get(0);
//                    PublicKey publicKey = getPublicKeyFromCert(x5c);
//                    keys.put(kid, publicKey);
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to fetch Google public keys", e);
//            }
//            return keys;
//        }
//
//        private PublicKey getPublicKeyFromCert(String cert) {
//            try {
//                byte[] bytes = Base64.getDecoder().decode(cert);
//                CertificateFactory factory = CertificateFactory.getInstance("X.509");
//                X509Certificate certificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
//                return certificate.getPublicKey();
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to get public key from certificate", e);
//            }
//        }
//    }
//}
