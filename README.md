# VNF Simulator

For starting the services:
```sh
java -jar vnf_simulator.jar
```
## Parameters

* \-\-domain
  Domain name
  Default: default
* \-\-help
  Prints this help :)
* \-\-iface \*
  Interface name
* \-\-instance \*
  Instance id (not the name)
* \-\-interval, -i
  Interval in milliseconds
  Default: 60000
* \-\-ip *
  Openstack host address
* \-\-metric
  The name of the metric
  Default: processing_time
* \-\-password
  Openstack password
  Default: lash5g
*\-\-proc_capacity
  Processing capacity in Mbit/s
  Default: 1.0
* \-\-project \*
  Project id (not the name)
* \-\-username
  Openstack username
  Default: lash5g


\*  **Required parameters**

For instance:
```sh
java -jar vnf_simulator.jar --project "9674deaa2c634743b089234a8226630a" --instance "7e49efff-ae7d-4140-9659-82c9e788d6b4" --iface "eth3" --interval 5000
```


### Notice
You can create a configuration file and pass it directly to the jar as follow:
```
java -jar vnf_simulator.jar @./config
```
Where the content of the "config" file is:
```
--iface
eth0
--instance
53bf5c9e-6312-45a8-a43d-4f2c95f550ad
--ip
192.168.0.1
--project
53bf5c9e27af987
```