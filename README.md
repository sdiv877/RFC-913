# RFC 913

## Setup

Although this implementation is designed to be cross-platform, and will work on any machine, the associated scripts are designed to be run on a linux system.

**Server Setup**

1. Open a new command prompt
2. Change directory to the `RFC 913/Server` directory
3. Execute the 'run' script with `./run.sh` to start the server
4. A message will be displayed stating that the server was successfully started

**Client Setup**

1. Open a new command prompt
2. Change directory to the `RFC 913/Client` directory
3. Execute the 'run' script with `./run.sh` to start the client
4. A message will be displayed stating that the client successfully connected to the server followed by a greeting from the server

Once a client has connected to the server, commands can be sent and the server will respond accordingly.

The user-id, their associated accounts and passwords are shown below. Please note that users 1-5 are used in the test script and the contents of their respective folders are not to be changed.

User-id|Account|Password
:---:|:---:|:---:
user1| | 
user2|acct1| 
user3| |pass3
user4|acct1|pass4
user5|acct1 acct2 acct3|pass5
user6|acct1 acct2 acct3 acct4|pass6

## Features

- Every command from the RFC 913 command has been successfully implemented and thoroughly tested
- The server fully supports multithreaded socket connections, each client connection will run on a unique thread
- Each user is assigned a unique folder on the server which they are locked to
- Relative and absolute filepaths are supported, the user's folder is translated as the root directory

## Testing

**Testing Setup**

1. Ensure the server is running
2. Open a new command prompt
3. Change directory to the `RFC 913/Client` directory
4. Execute the 'test' script with `./test.sh` to start the test script
5. The client will execute the test cases shown below

The output of the test script is shown below. Each command is tested thoroughly and has multiple test cases associated with it in order to ensure the command is working as intended. 

~~~
1. User-id valid
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> done
+Closing connection

2. User-id valid, account required
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user2
+User-id valid, send account and password
> acct acct1
!Account valid, logged-in
> done
+Closing connection

3. User-id valid, password required
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user3
+User-id valid, send account and password
> pass pass3
!Logged in
> done
+Closing connection

4. User-id valid, account and password required
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user4
+User-id valid, send account and password
> acct acct1
+Account valid, send password
> pass pass4
!Logged in
> done
+Closing connection

5. User-id valid, account and password required. Multiple accounts
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user5
+User-id valid, send account and password
> pass pass5
+Send account
> acct acct1
!Account valid, logged-in
> acct acct2
!Account valid, logged-in
> acct acct3
!Account valid, logged-in
> done
+Closing connection

6. User-id invalid
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user7
-Invalid user-id, try again
> done
+Closing connection

7. Account invalid
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user4
+User-id valid, send account and password
> acct acct2
-Invalid account, try again
> done
+Closing connection

8. Password invalid
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user4
+User-id valid, send account and password
> pass wrong
-Wrong password, try again
> done
+Closing connection

9. User-id, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user7 user7
ERROR: Invalid Arguments
Usage: USER user-id
> done
+Closing connection

10. Account, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user4
+User-id valid, send account and password
> acct acct2 acct2
ERROR: Invalid Arguments
Usage: ACCT account
> done
+Closing connection

11. Password, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user4
+User-id valid, send account and password
> pass wrong wrong
ERROR: Invalid Arguments
Usage: PASS password
> done
+Closing connection

12. Type
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> type a
+Using Ascii mode
> type b
+Using Binary mode
> type c
+Using Continuous mode
> done
+Closing connection

13. Type, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> type a a
ERROR: Invalid Arguments
Usage: TYPE { A | B | C }
> type d
-Type not valid
> done
+Closing connection

14. List standard, current directory
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> list f
+user1/
file2.txt
file.txt
file3.txt
file1.txt
temp
file4.txt
.DS_Store
data2.jpg
data.jpg
folder1
license.txt
> done
+Closing connection

15. List standard, other directory
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> list f temp
+user1/temp
file4.txt
file5.txt
data.csv
> done
+Closing connection

16. List verbose, current directory
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> list v
+user1/
Name: file2.txt    Path: user1/file2.txt    Size: 9 Bytes    
Name: file.txt    Path: user1/file.txt    Size: 16 Bytes    
Name: file3.txt    Path: user1/file3.txt    Size: 9 Bytes    
Name: file1.txt    Path: user1/file1.txt    Size: 8 Bytes    
Name: temp    Path: user1/temp    Size: 160 Bytes    
Name: file4.txt    Path: user1/file4.txt    Size: 8 Bytes    
Name: .DS_Store    Path: user1/.DS_Store    Size: 6148 Bytes    
Name: data2.jpg    Path: user1/data2.jpg    Size: 2345 Bytes    
Name: data.jpg    Path: user1/data.jpg    Size: 50300 Bytes    
Name: folder1    Path: user1/folder1    Size: 128 Bytes    
Name: license.txt    Path: user1/license.txt    Size: 11 Bytes    
> done
+Closing connection

17. List, non-existent directory
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> list f fake
-Cant list directory because: user1/fake does not exist
> done
+Closing connection

18. List file instead of directory
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> list f license.txt
-Cant list directory because: user1/license.txt is not a directory
> done
+Closing connection

19. List, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> list f / /
ERROR: Invalid Arguments
Usage: LIST { F | V } directory-path
> list g
-Argument error
> done
+Closing connection

20. Change directory, relative path
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> cdir folder1
!Changed working dir to user1/folder1
> cdir folder2
!Changed working dir to user1/folder1/folder2
> done
+Closing connection

21. Change directory, user root
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> cdir /
!Changed working dir to user1/
> done
+Closing connection

22. Change directory, absolute path
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> cdir /folder1/folder2
!Changed working dir to user1/folder1/folder2
> done
+Closing connection

23. Change directory, non-existent directory
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> cdir folder1/folder2/folder3
-Cant connect to directory because: user1/folder1/folder2/folder3 does not exist
> done
+Closing connection

24. Change directory, file instead of directory
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> cdir temp/data.csv
-Cant list directory because: user1/temp/data.csv is not a directory
> done
+Closing connection

25. Change directory, account required
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user2
+User-id valid, send account and password
> cdir folder1
+Directory exists, send account/password
> acct acct1
!Account valid, logged-in
!Changed working dir to user2/folder1
> done
+Closing connection

26. Change directory, password required
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user3
+User-id valid, send account and password
> cdir folder1
+Directory exists, send account/password
> pass pass3
!Logged in
!Changed working dir to user3/folder1
> done
+Closing connection

27. Change directory, account and password required
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user4
+User-id valid, send account and password
> cdir folder1
+Directory exists, send account/password
> acct acct1
+Account valid, send password
> pass pass4
!Logged in
!Changed working dir to user4/folder1
> done
+Closing connection

28. Change directory, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> cdir folder1 folder2
ERROR: Invalid Arguments
Usage: CDIR new-directory
> done
+Closing connection

29. Delete file
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> kill delete.txt
+user1/delete.txt deleted
> done
+Closing connection

30. Delete non-existent file
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> kill fake.txt
-Not deleted because user1/fake.txt does not exist
> done
+Closing connection

31. Delete, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> kill fake.txt fake.txt
ERROR: Invalid Arguments
Usage: KILL file-spec
> done
+Closing connection

32. Rename
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> name rename.txt
+File exists
> tobe new.txt
+user1/rename.txt renamed to user1/new.txt
> kill new.txt
+user1/new.txt deleted
> done
+Closing connection

33. Rename non-existent file
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> name fake.txt
-Can't find user1/fake.txt
> done
+Closing connection

34. Rename, file already exists
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> name file.txt
+File exists
> tobe file2.txt
-File wasn't renamed because user1/file2.txt already exists
> done
+Closing connection

35. Rename, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> name fake.txt fake.txt
ERROR: Invalid Arguments
Usage: NAME old-file-spec
> name file.txt
+File exists
> tobe new.txt new.txt
ERROR: Invalid Arguments
Usage: TOBE new-file-spec
> done
+Closing connection

36. Done
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> done
+Closing connection

37. Done, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> done done
ERROR: Invalid Arguments
Usage: DONE
> done
+Closing connection

38. Retrieve
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> retr temp/data.csv
+15 bytes will be sent
> send
+File sent
> done
+Closing connection

39. Retrieve, stop sending
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> retr temp/data.csv
+15 bytes will be sent
> stop
+File will not be sent
> done
+Closing connection

40. Retrieve non-existent file
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> retr fake.txt
-File doesn't exist
> done
+Closing connection

41. Retrieve directory instead of file
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> retr temp
-Specifier is not a file
> done
+Closing connection

42. Retrieve, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> retr file.txt file.txt
ERROR: Invalid Arguments
Usage: RETR file-spec
> retr file.txt
+16 bytes will be sent
> send send
ERROR: Invalid Arguments
Usage: SEND
> stop stop
ERROR: Invalid Arguments
Usage: STOP
> done
+Closing connection

43. Store new, file does not exist
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor new file.txt
+File does not exist, will create new file
> size 8
+Saved user1/file.txt
> done
+Closing connection

44. Store new, file exists
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor new file.txt
+File exists, will create new generation of file
> size 8
+Saved user1/file5.txt
> done
+Closing connection

45. Store old, file does not exist
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor old file.txt
+Will create new file
> size 8
+Saved user1/file.txt
> done
+Closing connection

46. Store old, file exists
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor old file.txt
+Will write over old file
> size 8
+Saved user1/file.txt
> done
+Closing connection

47. Store append, file does not exist
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor app file.txt
+Will create new file
> size 8
+Saved user1/file.txt
> done
+Closing connection

48. Store append, file exists
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor app file.txt
+Will append to file
> size 8
+Saved user1/file.txt
> done
+Closing connection

49. Store non-existent file
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor new fake.txt
ERROR: File doesn't exist
> done
+Closing connection

50. Store directory instead of file
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor new client
ERROR: Specifier is not a file
> done
+Closing connection

51. Store, argument error
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> user user1
!user1 logged in
> stor app file.txt file.txt
ERROR: Invalid Arguments
Usage: STOR { NEW | OLD | APP } file-spec
> stor a
ERROR: Invalid Arguments
Usage: STOR { NEW | OLD | APP } file-spec
> stor app file.txt
+Will append to file
> size 8 8
ERROR: Invalid Arguments
Usage: SIZE number-of-bytes-in-file
> done
+Closing connection

52. Access denied
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> type a
-Please log in first
> list f
-Please log in first
> name rename.txt
-Please log in first
> done
+Closing connection

53. Unknown command
Successfully connected to localhost on port 6789
+RFC 913 SFTP Server
> unknown
ERROR: Invalid Command
Available Commands: "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "TOBE", "DONE", "RETR", "SEND", "STOP", "STOR", "SIZE"
> done
+Closing connection
~~~