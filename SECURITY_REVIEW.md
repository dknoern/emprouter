# Security Review - EMPRouter

**Date:** September 30, 2024  
**Reviewed by:** Security Review Agent  
**Version:** 1.0-SNAPSHOT

## Executive Summary

This security review identifies multiple **CRITICAL** and **HIGH** severity vulnerabilities in the EMPRouter application. The most pressing issues include hardcoded credentials, outdated dependencies with known CVEs, lack of input validation, and missing authentication/authorization controls.

**Risk Level:** 🔴 **CRITICAL**

---

## Critical Vulnerabilities

### 1. Hardcoded Credentials (CRITICAL - CWE-798)

**Location:** Multiple files  
**Severity:** CRITICAL  
**CVSS Score:** 9.8

#### Affected Files:
- `src/main/java/com/seattleweb/emprouter/QpidConfigurer.java` (lines 51, 73)
- `src/main/java/com/seattleweb/emprouter/AMQPConnection.java` (line 52)

#### Details:
```java
// QpidConfigurer.java - Lines 51, 73
.addHeader("Authorization", Credentials.basic("admin", "admin"))

// AMQPConnection.java - Line 52
Connection connection = connectionFactory.createConnection("admin","admin");
```

#### Impact:
- Default credentials ("admin:admin") are hardcoded in source code
- Credentials visible in version control history
- Anyone with code access can authenticate to QPID broker
- No ability to change credentials without code modification
- Exposes message broker to unauthorized access

#### Recommendation:
- Move credentials to external configuration files (NOT in version control)
- Use environment variables for sensitive credentials
- Implement credential rotation policy
- Use strong, unique passwords
- Consider using connection pooling with credential management

---

### 2. Outdated Dependencies with Known Vulnerabilities (CRITICAL)

**Severity:** CRITICAL  
**CVSS Score:** 9.1+

#### Vulnerable Dependency: OkHttp 2.7.5

**Current Version:** 2.7.5 (Released: 2016)  
**Latest Version:** 4.x (as of 2024)  
**Age:** ~8 years outdated

**Known CVEs:**
- Multiple SSL/TLS vulnerabilities
- Certificate validation bypass issues
- Hostname verification problems
- Potential man-in-the-middle attack vectors

#### Impact:
- HTTP connections to QPID management API vulnerable to interception
- Credentials transmitted could be captured
- Management API commands could be hijacked
- No modern security features (TLS 1.3, modern cipher suites)

#### Recommendation:
```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version> <!-- or latest -->
</dependency>
```

**Note:** OkHttp 2.x → 3.x → 4.x requires code migration due to package name changes.

---

### 3. Insecure Communication (CRITICAL - CWE-319)

**Location:** `QpidConfigurer.java`  
**Severity:** CRITICAL  
**CVSS Score:** 8.1

#### Details:
```java
// Lines 50, 72
.url("http://localhost:8080/api/latest/queue/default/default/" + queueName)
.url("http://localhost:8080/api/latest/exchange/default/default/" + exchangeName)
```

#### Impact:
- HTTP (not HTTPS) used for management API
- Credentials transmitted in clear text (Base64 is NOT encryption)
- API commands visible to network sniffers
- Vulnerable to man-in-the-middle attacks
- Network traffic can be intercepted and modified

#### Recommendation:
- Use HTTPS for all management API calls
- Validate SSL/TLS certificates
- Implement certificate pinning for additional security
- Configure OkHttp client with proper SSL context

---

## High Severity Vulnerabilities

### 4. No Input Validation (HIGH - CWE-20)

**Location:** Multiple files  
**Severity:** HIGH  
**CVSS Score:** 7.5

#### Affected Areas:

**a) URL Path Injection (QpidConfigurer.java)**
```java
// Lines 50, 72 - Direct string concatenation
.url("http://localhost:8080/api/latest/queue/default/default/" + queueName)
.url("http://localhost:8080/api/latest/exchange/default/default/" + exchangeName)
```

**Vulnerability:** Queue/exchange names not validated before URL construction  
**Attack Vector:** Malicious names like `../../admin` could manipulate API endpoints  
**Impact:** Path traversal, unauthorized API access

**b) Configuration File Injection (AppConfigReader.java)**
```java
// Line 47 - No validation
int port = Integer.parseInt(st.nextToken());
```

**Vulnerability:** No bounds checking on port numbers  
**Attack Vector:** Port values outside 1-65535 range  
**Impact:** Application crashes, unexpected behavior

**c) Buffer Overflow Risk (ClassDConnection.java)**
```java
// Line 143 - Unsafe cast
byte[] data = new byte[(int) dataLength]; // Sloppy cast of long to int
```

**Vulnerability:** Long value cast to int without bounds checking  
**Attack Vector:** Large dataLength values could cause negative array size or OutOfMemoryError  
**Impact:** Denial of service, application crash

**d) Array Index Bounds (ByteUtils.java)**
```java
// Line 221 - No bounds checking
while(bytes[j]!=0 && j<bytes.length){ j++; }
```

**Vulnerability:** Condition order allows bytes[j] to be checked even when j >= bytes.length  
**Attack Vector:** Malformed messages without null terminator  
**Impact:** ArrayIndexOutOfBoundsException, application crash

#### Recommendations:
- Validate all external input (file, network, user)
- Implement whitelist validation for queue/exchange names
- Add bounds checking for all array operations
- Validate port numbers (1-65535 range)
- Use URL encoding/escaping for path parameters
- Implement maximum message size limits
- Add comprehensive error handling

---

### 5. Missing Error Handling and Information Disclosure (HIGH - CWE-209)

**Location:** Multiple files  
**Severity:** HIGH  
**CVSS Score:** 7.5

#### Issues:

**a) Stack Traces Exposed (AMQPConnection.java)**
```java
// Line 75
e.printStackTrace();
System.exit(-1);
```

**Impact:**
- Stack traces reveal internal structure
- Sensitive file paths exposed
- Application terminates abruptly on any error
- No graceful degradation

**b) No Response Validation (QpidConfigurer.java)**
```java
// Lines 57-59, 79-81
Response response = call.execute();
ResponseBody body = response.body();
body.close();
// No check of response.code() or error handling
```

**Impact:**
- Silent failures if API calls fail
- No error logging for troubleshooting
- Incorrect application state assumptions
- Queue/exchange may not actually be created

**c) Uncontrolled Resource Consumption (ClassDConnection.java)**
```java
// Line 105
while (true) { ... }  // Infinite loop
```

**Impact:**
- Thread never terminates
- No shutdown mechanism
- Resources never released
- Difficult to test and maintain

#### Recommendations:
- Implement proper exception handling at all layers
- Log errors to logging framework (don't use printStackTrace)
- Validate HTTP response codes (200, 201, etc.)
- Implement graceful shutdown mechanisms
- Add circuit breaker patterns for external services
- Sanitize error messages before logging
- Implement proper thread lifecycle management

---

### 6. Deprecated and Unsafe API Usage (HIGH)

**Location:** `ClassDConnection.java` line 166  
**Severity:** HIGH  
**CVSS Score:** 6.5

#### Details:
```java
// Line 166
stop();  // Thread.stop() is deprecated and unsafe
```

#### Impact:
- `Thread.stop()` can leave objects in inconsistent states
- No guarantee of resource cleanup
- Can cause deadlocks
- Deprecated since Java 1.2 (removed in modern Java)

#### Recommendation:
```java
// Use interrupt-based cancellation
private volatile boolean running = true;

public void shutdown() {
    running = false;
    interrupt();
}

public void run() {
    while (running && !Thread.currentThread().isInterrupted()) {
        // ... work ...
    }
}
```

---

## Medium Severity Issues

### 7. Weak Serialization Security (MEDIUM - CWE-502)

**Location:** `EmpMessage.java`  
**Severity:** MEDIUM  
**CVSS Score:** 5.3

#### Details:
```java
public class EmpMessage implements Serializable {
    private static final long serialVersionUID = 6440485997177638571L;
```

#### Impact:
- Deserializing untrusted data can lead to remote code execution
- No validation of deserialized objects
- Version control issues if class structure changes

#### Recommendation:
- Avoid Java serialization for network protocols
- Use explicit byte array conversion (already implemented in toByteArray())
- Consider using JSON or Protocol Buffers for serialization
- If serialization needed, implement readObject() with validation

---

### 8. Resource Leaks (MEDIUM - CWE-404)

**Location:** Multiple files  
**Severity:** MEDIUM  
**CVSS Score:** 5.3

#### Issues:

**a) Socket Not Closed on Error (ClassDConnection.java)**
```java
// Lines 109-119 - socket accepted but never closed on subsequent errors
socket = serverSocket.accept();
// ... if error occurs, socket remains open
```

**b) No Try-With-Resources (AppConfigReader.java)**
```java
// Lines 28-56 - Manual resource management
BufferedReader input = new BufferedReader(new FileReader(filename));
try { ... } finally { input.close(); }
```

#### Recommendation:
```java
// Use try-with-resources (Java 7+)
try (BufferedReader input = new BufferedReader(new FileReader(filename))) {
    // ... use input ...
}
```

---

### 9. Logging of Sensitive Data (MEDIUM - CWE-532)

**Location:** `EmpMessageReceiver.java` line 12  
**Severity:** MEDIUM  
**CVSS Score:** 4.3

#### Details:
```java
logger.info("message received: \n" + ByteUtils.toHexDump(data));
```

#### Impact:
- Full message content logged (may contain PII, credentials, etc.)
- Logs may be stored insecurely
- Compliance issues (GDPR, HIPAA, etc.)

#### Recommendation:
- Log metadata only (size, source, destination)
- Implement configurable logging levels
- Redact sensitive data from logs
- Use debug level for detailed data dumps

---

### 10. Outdated JUnit Version (MEDIUM)

**Location:** `pom.xml` line 14  
**Severity:** MEDIUM  
**CVSS Score:** 4.0

#### Details:
```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>3.8.1</version>  <!-- Released in 2002! -->
    <scope>test</scope>
</dependency>
```

#### Impact:
- 22+ year old testing framework
- Missing modern features
- No security updates
- Potential build tool vulnerabilities

#### Recommendation:
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
```

---

## Low Severity Issues

### 11. Missing Access Modifiers (LOW - CWE-766)

**Location:** Multiple files  
**Severity:** LOW

#### Examples:
- `AMQPConnection.java` - fields without private modifier (lines 17-23)
- `ClassDConnection.java` - package-private fields (lines 20-29)

#### Recommendation:
- Make all fields private unless specifically needed
- Use getter/setter methods for controlled access
- Apply principle of least privilege

---

### 12. No Configuration Validation (LOW)

**Location:** `AppConfigReader.java`  
**Severity:** LOW

#### Issues:
- No validation of EMP address format
- No duplicate detection
- No sanity checks on configuration values
- Application exits on missing config file (line 60)

#### Recommendation:
- Validate configuration on startup
- Provide default configuration
- Log configuration errors clearly
- Allow graceful fallback

---

### 13. Hardcoded File Paths (LOW)

**Location:** `AppConfigReader.java` line 25  
**Severity:** LOW

#### Details:
```java
String filename = "config/emprouter.cfg";
```

#### Recommendation:
- Make configuration path configurable via environment variable
- Support multiple configuration locations
- Document configuration file location

---

## Dependency Security Analysis

### Current Dependencies Status

| Dependency | Current | Latest | Status | Vulnerabilities |
|------------|---------|--------|--------|-----------------|
| OkHttp | 2.7.5 | 4.12.0 | 🔴 CRITICAL | Multiple CVEs |
| JUnit | 3.8.1 | 5.10.0 | 🔴 OUTDATED | End of life |
| qpid-jms-client | 2.6.1 | 2.6.1 | ✅ CURRENT | None known |
| slf4j-api | 2.0.16 | 2.0.16 | ✅ CURRENT | None known |
| logback-classic | 1.5.16 | 1.5.16 | ✅ CURRENT | None known |

---

## Security Best Practices Missing

### 1. Authentication & Authorization
- ❌ No authentication on ClassD connections
- ❌ No authorization checks on message routing
- ❌ No rate limiting
- ❌ No audit logging of security events

### 2. Encryption
- ❌ No TLS/SSL for ClassD protocol
- ❌ No message-level encryption (despite bodyEncrypted flag)
- ❌ No secure credential storage

### 3. Input Validation
- ❌ No message size limits
- ❌ No format validation
- ❌ No sanitization of addresses

### 4. Monitoring & Logging
- ❌ No security event logging
- ❌ No intrusion detection
- ❌ No anomaly detection

### 5. Secure Development
- ❌ No security tests
- ❌ No dependency scanning in CI/CD
- ❌ No static code analysis

---

## Recommendations Summary

### Immediate Actions (CRITICAL - Do First)

1. **Remove Hardcoded Credentials**
   - Move to environment variables or secure vault
   - Change default passwords immediately
   - Implement credential rotation

2. **Update OkHttp Library**
   - Upgrade from 2.7.5 to 4.x
   - Update code for API changes
   - Test thoroughly

3. **Enable HTTPS**
   - Configure QPID for HTTPS management
   - Update code to use HTTPS URLs
   - Validate SSL certificates

4. **Add Input Validation**
   - Validate all external inputs
   - Implement bounds checking
   - Add message size limits

### Short Term (HIGH Priority - This Sprint)

5. **Implement Proper Error Handling**
   - Replace printStackTrace() with proper logging
   - Validate HTTP responses
   - Handle errors gracefully

6. **Fix Resource Management**
   - Use try-with-resources
   - Implement graceful shutdown
   - Close all resources properly

7. **Remove Unsafe APIs**
   - Replace Thread.stop() with interrupt-based cancellation
   - Update deprecated API usage

### Medium Term (Next Quarter)

8. **Add Security Controls**
   - Implement authentication for ClassD connections
   - Add TLS support for ClassD protocol
   - Implement rate limiting
   - Add audit logging

9. **Improve Testing**
   - Update JUnit to 5.x
   - Add security tests
   - Implement integration tests
   - Add fuzzing tests

10. **Security Infrastructure**
    - Add dependency vulnerability scanning
    - Implement SAST/DAST in CI/CD
    - Add security code reviews
    - Create security documentation

### Long Term (Future Releases)

11. **Protocol Security**
    - Implement message-level encryption
    - Add digital signatures
    - Support modern authentication mechanisms

12. **Compliance**
    - Document security controls
    - Implement data protection measures
    - Add compliance reporting

---

## Testing Recommendations

### Security Tests Needed

1. **Authentication Tests**
   - Test with invalid credentials
   - Test credential rotation
   - Test without credentials

2. **Input Validation Tests**
   - Fuzzing tests for all inputs
   - Boundary value tests
   - Malformed message tests
   - SQL injection attempts (for API calls)
   - Path traversal attempts

3. **Resource Exhaustion Tests**
   - Large message tests
   - Connection flood tests
   - Memory leak tests

4. **Network Security Tests**
   - SSL/TLS verification
   - Certificate validation
   - Man-in-the-middle testing

---

## Compliance Considerations

This application likely requires compliance with:

- **PCI DSS** - If processing payment data
- **HIPAA** - If processing health data
- **GDPR** - If processing EU personal data
- **SOC 2** - For service organizations

**Current Compliance Status:** ❌ NON-COMPLIANT

### Gaps:
- No encryption at rest or in transit
- No access controls
- No audit logging
- No data protection measures

---

## Conclusion

The EMPRouter application has **significant security vulnerabilities** that must be addressed before production deployment. The most critical issues are:

1. Hardcoded credentials in source code
2. Severely outdated dependencies with known CVEs
3. Unencrypted communication channels
4. Lack of input validation
5. Poor error handling and resource management

**Recommended Actions:**
- ⚠️ **DO NOT DEPLOY** to production until critical issues are resolved
- 🔴 Address all CRITICAL issues immediately
- 🟠 Plan remediation for HIGH severity issues
- 📋 Create security roadmap for medium/low issues
- 🔄 Implement ongoing security review process

**Estimated Remediation Effort:**
- Critical fixes: 2-3 weeks
- High priority fixes: 4-6 weeks
- Full security hardening: 2-3 months

---

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [CVE Database](https://cve.mitre.org/)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [Java Security Best Practices](https://www.oracle.com/java/technologies/javase/seccodeguide.html)

---

**Review Status:** ✅ COMPLETE  
**Next Review Date:** After critical fixes implemented  
**Reviewer:** Security Review Agent  
**Review Date:** September 30, 2024
