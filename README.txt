Names: 
Mitsu Deshpande
NetID: mad130430
Chitra Harihara Pranadarthan
NetID: cxh141330

Steps to execute the program:

1. unzip the zipped folder project2.zip in any directory

unzip project2.zip

2. Copy the config file(<config-file>.txt) to the unzipped folder
cp *.txt project2

3. cd project

4. Change permission for launcher.sh and cleanup.sh to execute

5. Execute the launcher.sh file and pass config-file and netid as arguments
bash launcher.sh <config-file> <netid>

After executing the launcher, there will be sysout statements displayed on the console. Once a FINISH message is sent, there will be sysout statements displayed on the console which says Terminating Node <id>  
In case there is a connection refused error, please try with different port numbers.

6. Execute the cleanup.sh file and pass config-file and netid as arguments 
bash cleanup.sh <config-file> <netid>