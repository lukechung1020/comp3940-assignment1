# Assignment 1 for COMP 3940

## Setup

1. Download [tomcat 10](https://tomcat.apache.org/download-10.cgi).

2. Unzip tomcat to a known location.

3. For ease of use, rename the unzipped tomcat to **tomcat**.

4. In the `~/tomcat/webapps` directory clone this repository.

5. In the `WEB-INF` directory, add a file named `db.properties`

   - Inside the `db.properties` file, add the following:

   ```
       db.url=your_database_url
       db.username=your_username
       db.password=your_password
   ```

6. To setup your mysql tables, run the commands in `sqlcommands.sql`.

7. Compile all '.java' files in the '/classes' directory using `javac -cp ".;path/to/tomcat/lib/servlet-api.jar;path/to/mysql-connector-j-x.x.x.jar;path/to/jbcrypt.jar;path/to/json-20240303.jar" *.java`

8. Open the `~/tomcat/bin directory` in **CMD** or **Terminal** and run the command: `startup.bat` for Windows and `sh startup.sh` for Mac.

9. Once the tomcat server has been started go to `http://localhost:8081/comp3940-assignment1/signup` to create an account.

10. After you have created an account, you must change one of the column values in table `users`. To access all features change `user_type` to `admin`. 

## Contributors

- Luke Chung
- Sarah Liu
- Jimmy Tsang
