# Introduction

This artifact can be evaluated through the following experiments for:
- Comparing our tool VeriLin (Section 1.1) with the other 3 tools (Section 1.2) for linearizability checking, as reported in Section 7.1 of the paper we submitted;
- (Section 2) Manual mode of VeriLin with the two large-scale case studies, as reported in Sections 7.2 & 7.3 of the paper we submitted;
- (Section 3) Automatic mode of VeriLin, as shown in Section 6 of the paper submitted.

The all-in-one package for this artifact is available at https://lcs.ios.ac.cn/~lvyi/files/VeriLin.tgz, which is of size 3.1GB.

### Notes

- The experiments reported in the submitted paper are conducted on an Ubuntu 18.04 machine with 96 cores and 2048G memory. Java SE 11 is used for all these experiments except the tools comparison part, which uses Java SE 14. 
- We recommend to run these experiments with at least 16 cores and about 256G memory. With less resources, the results may differ from those reported in the submitted paper. 
- For all these experiments, concurrent histories are to be generated in a random manner. Thus, the experimental results may vary from time to time. We report the average statistics to reduce possible bias in the following experiments and in the submitted paper. However, we envisage that more tests may demonstrate the performance of our and others' approaches more accurately.
- Warnings may be reported during executing an experiment, e.g. "warning: Unsafe is internal proprietary API and may be removed in a future release". These warnings concern only about the concurrent object under test, instead of the implementation of VeriLin.
- The evaluations of Sections 2 and 3 are fully included in this GitHub repository, and do not require the external dependencies. The evaluation of section 1 require docker and other dependencies.

## 1. Comparing VeriLin with the other 3 tools (TFL, lincheck and VeriTrace)
In this artifact, VeriLin, TFL and lincheck can be run directly, but VeriTrace is hard-coded and dependent on the specifically configured JPF. We use a docker to establish the run-time environment for VeriTrace. The results of this section correspond to those reported in Figure 4 of the submitted paper.

Before running the experiment of Figure 4, please download non_docker_env.tgz (is available at https://drive.google.com/file/d/1Mx8iWjmfNFBcrCt5YRjeLUchzX0Y7P8G/view?usp=sharing, size 1.8G)  and toolComparison.tgz (is available at https://drive.google.com/file/d/11-Go5kw8dKfrmhMrIDDytqEGknfSAUjm/view?usp=sharing, size 1.3G) for the above dependencies and executable files. Decompress these two files in the root directory ***VeriLin***.
```bash
    tar -xvzf non_docker_env.tgz
    tar -xvzf toolComparison.tgz
```
### 1.1 VeriLin

The result is shown in ***result_Figure4_VeriLin*** by running run_comprison_VeriLin_v1.sh, in the format below.

```
tool	Object	Size	#Solved
VeriLin	oplist	4 * 100000	7/10
VeriLin	oplist	4 * 10000	9/10
VeriLin	oplist	4 * 1000	10/10
VeriLin	lazylist	4 * 100000	10/10
VeriLin	lazylist	4 * 10000	10/10
VeriLin	lazylist	4 * 1000	10/10
VeriLin	lockfreestack	4 * 100000	10/10
VeriLin	lockfreestack	4 * 10000	10/10
VeriLin	lockfreestack	4 * 1000	10/10
VeriLin	lockfreequeue	4 * 100000	10/10
VeriLin	lockfreequeue	4 * 10000	10/10
VeriLin	lockfreequeue	4 * 1000	10/10
VeriLin	concurrentdeque	4 * 100000	10/10
VeriLin	concurrentdeque	4 * 10000	10/10
VeriLin	concurrentdeque	4 * 1000	10/10
```

This experiment can be executed with either of the following three settings:
***once: only 1 time per object***
```bash
    sudo sh run_comparison_VeriLin.sh
```
***short: repeated for 10 times per object***
```bash
    sudo sh run_comprison_VeriLin_v1.sh
```
***complete: repeated for 100 times per object***
```bash
    sudo sh run_comprison_VeriLin_v2.sh
```

### 1.2 TFL, lincheck and VeriTrace

The result is shown in ***result_Figure4_Others*** by running run_comparison_Others.sh, in the format below.

```
,method,case,size,pass
0,TFL,Lock-free Queue,4*10000,0/1
1,TFL,Lock-free Queue,4*100000,0/1
2,TFL,Lazy List,4*10000,0/1
3,TFL,Lazy List,4*100000,0/1
4,TFL,Optimistic List,4*10000,0/1
5,TFL,Optimistic List,4*100000,0/1
6,TFL,Lock-free Stack,4*10000,0/1
7,TFL,Lock-free Stack,4*100000,0/1
,method,case,size,pass
0,lincheck,Lock-free Queue,4*10,0/1
1,lincheck,Lock-free Stack,4*10,0/1
2,lincheck,Lazy List,4*10,0/1
3,lincheck,Optimistic List,4*10,0/1
4,lincheck,Lock-free Queue,4*5,1/1
5,lincheck,Lock-free Stack,4*5,1/1
6,lincheck,Lazy List,4*5,1/1
7,lincheck,Optimistic List,4*5,1/1
,method,case,size,pass
0,VeriTrace,Concurrent Double Ended Queue,4*10,0/1
1,VeriTrace,Concurrent Double Ended Queue,4*100,0/1
2,VeriTrace,Concurrent Double Ended Queue,4*5,0/1
3,VeriTrace,Lazy List,4*10,1/1
4,VeriTrace,Lazy List,4*100,0/1
5,VeriTrace,Lazy List,4*5,1/1
6,VeriTrace,Lock-free Stack,4*10,0/1
7,VeriTrace,Lock-free Stack,4*100,0/1
8,VeriTrace,Lock-free Stack,4*5,0/1
9,VeriTrace,Optimistic List,4*10,0/1
10,VeriTrace,Optimistic List,4*100,0/1
11,VeriTrace,Optimistic List,4*5,0/1
```

This experiment can be executed with either of the following three settings:

***once: only 1 time per object***
```bash
    sudo sh run_comparison_Others.sh
```
***short: repeated for 10 times per object***
```bash
    sudo sh run_comparison_Others_v1.sh
```
***complete: run 100 times per object***
```bash
    sudo sh run_comparison_Others_v2.sh
```


## 2. Manual mode of VeriLin with the two large-scale case studies

The results of this section correspond to those reported in Table 1, Table 2 and Table 3 in the submitted paper.   

### 2.1 Ticketing System

#### 2.1.1 ratio of invoke time VS. response Time

For this experiment only, run
```bash
    ./run_table1.sh
```
An intermediate result is saved in ***result_Table1***, and can be shown in the format below by running
```bash
    ./show_table1.sh
```

```
Scale 100 4 250 0 // Size 4*250, Delay 0ms, repeat for 100 times
Completed: 103 ; Ratio: 7 15 63 13 5 ; Timeout: 202 177 136 
Scale 100 4 500 0
Completed: 103 ; Ratio: 6 15 64 10 8 ; Timeout: 207 182 141 
Scale 100 4 1000 0
Completed: 103 ; Ratio: 7 17 68 4 7 ; Timeout: 215 178 142 
Scale 100 4 2000 0
Completed: 103 ; Ratio: 8 15 68 9 3 ; Timeout: 207 181 138 
```
The above is also the final result when ***run_table1.sh*** finishes with the above 4 scales (running in parallel)，which could be very time- and space-consuming. "Completed: 103" means all the 103 implementations are tested under the given scale. To repeat a single scale experiment for x times with n threads, m operations per thread, delay t milliseconds, run
```bash
    ./run_table1_single.sh x n m t
```
An intermediate result can be shown with ***show_table1_single.sh***.
```bash
    ./show_table1_single.sh x n m t
```

For example, to repeat a single experiment of size 4*125, delay 0ms, for 100 times, run
```bash
    ./run_table1_single.sh 100 4 125 0
    ./show_table1_single.sh 100 4 125 0
```
#### 2.1.2 waiting time and linearizability checking

For this experiment only, run
```bash
    ./run_table2.sh
```

An intermediate result is saved in ***result_Table2***, and can be shown in the following format by running
```bash
    ./show_table2.sh
```

```
Scale 100 4 250 0
Completed: 103 ; Non-lin: 2034 ; Buggy Imp: 40 ; Time: 3d4h48m39s
Scale 100 4 250 1
Completed: 103 ; Non-lin: 2024 ; Buggy Imp: 31 ; Time: 2h23m45s
Scale 100 4 250 10
Completed: 103 ; Non-lin: 2007 ; Buggy Imp: 26 ; Time: 2h11m52s
Scale 100 4 500 0
Completed: 103 ; Non-lin: 2038 ; Buggy Imp: 40 ; Time: 3d3h36m0s
Scale 100 4 500 1
Completed: 103 ; Non-lin: 2027 ; Buggy Imp: 33 ; Time: 2h52m56s
Scale 100 4 500 10
Completed: 103 ; Non-lin: 2026 ; Buggy Imp: 26 ; Time: 2h41m25s
Scale 100 4 1000 0
Completed: 103 ; Non-lin: 2052 ; Buggy Imp: 46 ; Time: 3d16h50m36s
Scale 100 4 1000 1
Completed: 103 ; Non-lin: 2062 ; Buggy Imp: 33 ; Time: 3h49m35s
Scale 100 4 1000 10
Completed: 103 ; Non-lin: 2065 ; Buggy Imp: 28 ; Time: 3h29m13s
Scale 100 4 2000 0
Completed: 103 ; Non-lin: 2078 ; Buggy Imp: 46 ; Time: 4d7h33m22s
Scale 100 4 2000 1
Completed: 103 ; Non-lin: 2085 ; Buggy Imp: 35 ; Time: 5h39m13s
Scale 100 4 2000 10
Completed: 103 ; Non-lin: 2137 ; Buggy Imp: 27 ; Time: 4h58m22s
```

The above is also the final result when ***run_table2.sh*** finishes with the above 12 scales. "Non-lin" = #Non-linearizable Histories, "Buggy Imp" = #Buggy Implementations. To repeat a single scale experiment for x times with n threads, m operations per thread, delay t milliseconds, run
```bash
    ./run_table2_single.sh x n m t
```
An intermediate result can be shown with ***show_table2_single.sh***.
```bash
    ./show_table2_single.sh x n m t
```

### 2.2 Multicore OS module

For this experiment only, run
```bash
    ./run_table3.sh
```

Please note that the jar packages of the Multicore OS module do not work correctly under Java SE 8. The result is shown in ***result_Table3***, in the following format. 

```
Scale 100 4 125 0
Completed: 100 ; RegionNum: 74 ; MaxRegion: 45 ; Solved: 28 ; Time: 1d12h0m23s
Scale 100 4 125 1
Completed: 100 ; RegionNum: 162 ; MaxRegion: 6 ; Solved: 41 ; Time: 1d7h41m49s
Scale 100 4 125 10
Completed: 100 ; RegionNum: 188 ; MaxRegion: 3 ; Solved: 88 ; Time: 7h22m15s
Scale 100 4 250 0
Completed: 100 ; RegionNum: 141 ; MaxRegion: 66 ; Solved: 10 ; Time: 1d21h27m15s
Scale 100 4 250 1
Completed: 100 ; RegionNum: 339 ; MaxRegion: 7 ; Solved: 30 ; Time: 1d14h2m26s
Scale 100 4 250 10
Completed: 100 ; RegionNum: 375 ; MaxRegion: 3 ; Solved: 72 ; Time: 15h42m50s
Scale 100 4 500 0
Completed: 100 ; RegionNum: 183 ; MaxRegion: 49 ; Solved: 7 ; Time: 1d23h22m55s
Scale 100 4 500 1
Completed: 100 ; RegionNum: 694 ; MaxRegion: 5 ; Solved: 26 ; Time: 1d15h58m22s
Scale 100 4 500 10
Completed: 100 ; RegionNum: 754 ; MaxRegion: 3 ; Solved: 71 ; Time: 16h40m3s
Scale 100 4 1000 0
Completed: 100 ; RegionNum: 818 ; MaxRegion: 57 ; Solved: 3 ; Time: 2d0h38m38s
Scale 100 4 1000 1
Completed: 100 ; RegionNum: 1356 ; MaxRegion: 7 ; Solved: 21 ; Time: 1d17h56m48s
Scale 100 4 1000 10
Completed: 100 ; RegionNum: 1529 ; MaxRegion: 3 ; Solved: 70 ; Time: 16h44m22s
```

The above is the final result when ***run_table3.sh*** finishes with the above 12 scales, 100 runs per scale. "RegionNum" = #Regions, shows the average number of distinct regions in a solved history. "MaxRegion" = Max Region Size, shows the average size of the largest region in a solved history. The number of threads for running the mulitcore OS module is fixed to 4. To repeat a single scale experiment for x times with m operations per thread, delay t milliseconds, run
```bash
    ./run_table3_single.sh x 4 m t
```
An intermediate result can be shown with ***show_table3_single.sh***.
```bash
    ./show_table3_single.sh x 4 m t
```
## 3. Automatic mode of VeriLin

The automatic mode of VeriLin is reported in Section 6 in the submitted paper. This section demonstrates how it works with an example of list. The result is shown in ***result_auto***, in the form shown below. 

For this experiment only, run
```bash
    ./run_automode.sh
```

```
test0:
Verification Finished. 223ms
test1:
Verification Failed. 296ms
test2:
Verification Finished. 218ms
test3:
Verification Finished. 230ms
test4:
Verification Finished. 218ms
test5:
Verification Finished. 222ms
test6:
Verification Finished. 220ms
test7:
Verification Finished. 216ms
test8:
Verification Finished. 219ms
test9:
Verification Finished. 232ms
test10:
Verification Finished. 227ms
test number = 10, failed number = 1
```
