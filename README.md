# Gatling (Java biding)
Performance and Load testing for REST apis

### Starting PetClinic API
[PetClinic Github](https://github.com/spring-petclinic/spring-petclinic-rest)
```
git clone https://github.com/spring-petclinic/spring-petclinic-rest.git
cd spring-petclinic-rest
./mvnw spring-boot:run
```

### Preconditions for test execution:
- [x] JDK 8+
- [x] MAVEN 3.5+
- [x] PetClinic API running
- [x] Swagger docs opens at http://localhost:9966/petclinic

### Running tests:
```
mvn clean gatling:test
```

### Running specific simulation:
```
mvn clean gatling:test -Dgatling.simulationClass=simulations.OwnersSimulation
```
### Running with different config (new rows added for readability):
```
mvn clean gatling:test 
-Dusername=user 
-Dpassword=secret 
-DbaseUrl=https://pragmatic.bg 
-Dusers=10 
-Dduration=60
```