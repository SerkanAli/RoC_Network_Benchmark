# RoC_Network_Benchmark
This Repo is for the course of RoC on TU Berlin in Summer Semester 19/20

This Java application is for benchmarking different Network Protocols. 
The main structure is based on Client and Server schema, where Client is constantly sending data to Server. 
The benchmarking results are written into a csv file. Implemented protocols are:
-TCP (Standart Java Library)
-UDP (Standart Java Library)
-MQTT (Eclipse Paho Library)

Various input Parameter can be changed:
-Used maximum and minimum thread number: The Benchmark will begin from minimum number and will test every possible parallel sending 
up to max number of threads
-Maximum and Minimum sending File Size: Begin from minimum File size, 
will increase the file size exponential up to max file size. The files are byte arrays
-Iteration number: How often should send the same file on established connection
-Sleep Time: To calm down between Thread count switch
-IP Adress of the Server

Ports 1889, and all between 6290 - 6330 should be free for use
