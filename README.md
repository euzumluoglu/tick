
##### tick

In memory Instrument price calculator program which is written spring-boot
#### 1) Requirements
JDK11 or above must be installed in local environment.
#### 2) How to Run
You can run the command below on root directory 
```terminal
mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx8192m"
```

#### 3) __Api Documentation__
&nbsp;
##### 3.1) __List of urls__

javamelody access `http://localhost:8080/monitoring`

Send a `POST` request to `http://localhost:8080/ticks` add a new price

Sample Request Body
```json
{   
    "instrument": "IBM.N",   
    "price": 143.82,   
    "timestamp": 1478192204000   
}
```
Expected result
| http status | meanining | explanation |
 | :---: | :---: | :---: |
 | 201 | price successfully updated |timestamp is valid and within current 60 second  |
 | 204 | price is invalid |timestamp is older than 60 seconds or ahead of current time  |

Send a `GET` Request to `http://localhost:8080/statistics` to get general summary

Sample Response Body
```json
{   
    "avg": 100,   
    "max": 200,   
    "min": 50,   
    "count": 10   
} 
```

Field explanations are listed in the table below

| field | explanation |
 | :---: | :---: |
 | count | number of new price update for all instruments within 60 seconds  |
 | max | maximum price in all instruments within 60 seconds  |
 | min | minimum price in all instruments within 60 seconds  |
 | avg | total price / count for all instruments within 60 seconds  |
 
 Send a `GET` Request to `http://localhost:8080/statistics/{instrument_identifier}` to get the summary of specific instrument

Sample Response Body
```json
{   
    "avg": 70,   
    "max": 35,   
    "min": 10,   
    "count": 4   
} 
```

Field explanations are listed in the table below

| field | explanation |
 | :---: | :---: |
 | count | number of new price update for the instrument within 60 seconds  |
 | max | maximum price of the instrument within 60 seconds  |
 | min | minimum price of the instrument within 60 seconds  |
 | avg | total price / count for the instrument within 60 seconds  |
 
#### 4) __Assumptions__

   - Each price calculated based on seconds. Nanoseconds are omitted during calculation. (19:08:21+01:00,232 consider as 19:08:21+01:00)
   - It is expected that multiple price update request can come for the same instrument within the same second interval. Both of them added for the same second. 
   - Price with future timestamp is considered as invalid request and evaluted in the same context of prices which is older than 60 seconds.
