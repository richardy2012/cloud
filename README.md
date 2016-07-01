# Distributed scheduling system architecture and datashift illustration   
===============================
* distributed job scheduling
* distributed trigger job scheduling

# Goal
1. Data parallel
1. fault-tolerant
1. No single point
1. callback
1. Framework control section general logic

![System Architecture](https://github.com/wbj0110/cloud/blob/master/doc/image/core/System%20Architecture.png)   
![Job](https://github.com/wbj0110/cloud/blob/master/doc/image/core/job_entity.png)   
## ZK+Leader trigger for cron and dependency schedule  
#### Node
1.   create /root/jobs/workerid node in zookeeper and watch it
1.   receive job from client
1.   check if jobname is unique from the children of  zookeeper /root/jobname  
1.   read /root/job and caculate job list size of everey workerid
1.   banlace by job list size and submit job to /root/job/workerid

1. Every worker  automatic  synchronize data by watch /root/job/workerid  and  schedule  the job to  distributed queue  /root/queue/job in zookeeper by clock

#### If Node is  Leader(Master)
  *   Need watch /root/job by TreeNodeCache  ,get all job  and  cache it ,draw a linked list for all job to make  current job know  the next job  list that dependency it
  *   On job finished  if has  been dependencyed ,then  schedule  the next job  list to  distributed queue in zookeeper right now    
   
      
***
![ZK+Leader trigger for cron and dependency schedule](https://github.com/wbj0110/cloud/blob/master/doc/image/core/Zk%2BLeader%20trigger%20for%20cron%20and%20dependency%20schedule%20watch.png)   
   
#### Job submit logical   
* Submit job by http
* Receive job from jetty servlet and submit to zk (jobManager) /root/jobs/worker-xxx (balance)
* Node watch /root/jobs/worker-xxx and receive job to local cache
* Schedule to cron timer
* Submit job to distribute queue(default zk queue)
* Leader consume job from queue   
       
![Diagrammatize Job Submited](https://github.com/wbj0110/cloud/blob/master/doc/image/core/Diagrammatize%20job%20submited.png)   

---
#### ZK queue + Master(Leader) Job and Resource Schedule 

#####Master:
* Before  taking leadership
* Try to create the master znode
* If it goes through, then take leadership
* Upon connection loss, needs  to check if znode is there and who owns it
* Upon determining that someone else owns it, watch the master znode
* After taking leadership
* Get workers
* Set a watch on the list of workers
*               On workers are deleted ,need get unassign job from /root/assign/worker-xxx and reassign it and  move /job/workerid-xx(dead) got active worker and blance it 
* Check for dead workers and reassign tasks
* For each dead worker
* Get assigned tasks
* Get task data
* Move task to the list of unassigned tasks
* Delete assignment
* Recover tasks (tasks assigned to dead workers)
* Get unassigned tasks and assign them
* For each unassigned task
* Get task data
* Choose worker
* Check the resource(mem,cpu.active threads count …)[Set a watch on /root/resources/work-xxx]
* Assign to worker
* Delete task from the list of unassigned
*                Consumer  job from queue  /root/queue/job/
*                Set  a watch (TreeCacheNode) on  /root/job  
*                         Update the all jobs to localcache  and  generate a linked list tree for dependency job  schedule
*                Set a watch on  /root/status  
* 	Caculate the status of job 
*          	Save job’s status to zookeeper
* 	Persist all  job’s status to redis
*                         
* Worker[Include master]:
* Creates  /root/assign/worker-xxx znode
* Creates  /root/workers/worker-xxx znode
* Watches  /root/assign/worker-xxx znode
* Set a watch on /root/assign/worker-xxx
* Get tasks upon assignment
* For each task, get task data
* Execute task data
* Create status
* Delete assignment
* Set watch on  /root/cluster_status
* Set watch on  /root/configs


    
![Master Procedure Summarize](https://github.com/wbj0110/cloud/blob/master/doc/image/core/master%20procedure%20summarize.png)

![HA](https://github.com/wbj0110/cloud/blob/master/doc/image/core/HA.png)
     
    
####Master Logical   
1. Receive job from distribute queue
1. Resources check and assgin tast 
1. Status(job and partition status ,cluster status[active size and host]) manager and Web UI show
   
####Node Logical   
1.Connect to Zookeeper And Get unique increment id for self by zk 
2.Set Watch list /root/assgin/worker-xxx
3.Set Watch list /root/jobname for check unique job
4.Set Watch list /root/jobs/worker-xxx for timer schedule 
5.Receive job submitted by rest service
6.Check if exists for current jobname
7.Read size of jobs from zk (/root/jobs every worker)
8.Submit job to the correct worker
9.Schedule job by trigger (cron or dependency ) from local
10.Submit job to distribute queue by trigger from loacal worker
----------------------------------
1.Get Partition Task And add to threadPool for execute
2.Report status to zk(Leader will watch it ),when started,finished,exception,systemException and so on

####Ensure jobname is unique    
* val names =Set(jobname)
* Submit job and get jobname
* Add jobname to names and if names.size did’t change,it’s not unique else submit jobname to zk /root/jobname
* Node watch /root/jobname and update the set names
   
![ZK queue + Master(Leader) Job and Resource Schedule ](https://github.com/wbj0110/cloud/blob/master/doc/image/core/ZK%20queue%20%2B%20Master%20Job%20and%20Resource%20Schedule.png)
    
![Zookeeper统一配置中心](https://github.com/wbj0110/cloud/blob/master/doc/image/core/Zookeeper%E7%BB%9F%E4%B8%80%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83.png)
   
![Logical of resource allocation](https://github.com/wbj0110/cloud/blob/master/doc/image/core/Logical%20of%20resource%20allocation.png)    
![Job Status](https://github.com/wbj0110/cloud/blob/master/doc/image/core/Job%20Status.png)    

![Watch](https://github.com/wbj0110/cloud/blob/master/doc/image/core/Watch.png)      
![Watch1](https://github.com/wbj0110/cloud/blob/master/doc/image/core/Watch1.png)    
![check whether updated by guys，Not Cover](https://github.com/wbj0110/cloud/blob/master/doc/image/core/check%20whether%20updated%20by%20guys.png)     

###DatashiftClassDiagram   

![DatashiftClassDiagram](https://github.com/wbj0110/cloud/blob/master/doc/image/datashift.png)


