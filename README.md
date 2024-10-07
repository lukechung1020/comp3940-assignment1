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

7. Copy all jar files in `WEB-INF/lib` into `path/to/tomcat/lib`.

8. Compile all '.java' files in the `WEB-INF/classes` directory using `javac -g -cp "path/to/tomcat/lib/*" *.java`

9. Add the following two lines in the 'catalina.bat' file in the bin folder of tomcat. Make sure that the paths make sense for your installation of tomcat.

   set "JAVA_OPTS=%JAVA_OPTS% -javaagent:c:\tomcat\lib\aspectjweaver-1.9.22.1.jar"

   set "CLASSPATH=%CLASSPATH%;C:\tomcat\webapps\comp3940-assignment1\WEB-INF\classes"
   
 
The above lines should be added just above where you see the following line in the file.
```rem ----- Execute The Requested Command ---------------------------------------```

10.Open the `~/tomcat/bin directory` in **CMD** or **Terminal** and run the command: `startup.bat` for Windows and `sh startup.sh` for Mac.

11.Once the tomcat server has been started go to `http://localhost:8081/comp3940-assignment1/signup` to create an account.

12.After you have created an account, you must change one of the column values in table `users`. To access all features change `user_type` to `admin`. 

## Contributors

- Luke Chung
- Sarah Liu
- Jimmy Tsang
