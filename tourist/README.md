#  设计

<pre>

                                             SignalingAdapter
                                                     |    
                                                     |      Signaling [SignalingEvent]
                                                     |                                
                           +-------------------------+--------------------------+
                           |                                                    |                                                 
                           |                                                    |                                                 
                           |  daytime StayHoursPE                               |night StayHoursPE                                
                           |                                                    |                                                 
                           | one pe instance per imsi                           |     one pe instance per imsi                    
                           |                                                    |                                                 
          +------------+---+--------+----------                  +---------+--------+------+--------                              
          |            |            |                            |         |               |           
          |            |            |                            |         |               |             ->>---------->>>>>-------+
    StayHoursPE   StayHoursPE    StayHoursPE                StayHoursPE StayHoursPE     StayHoursPE                               |
          |            |            |                            |         |               |             -<<--TimeUpdateEvent-----+
          |            |            |                            |         |               |                                      |
         -+  -- -- -- -+ -- -- -- --|-- --  [StayHoursEvent]  -- | --  -- -+ -- -- -- -- --|-- --                                 |
          |            |            |                            |         |               |                                      |
          |            |            |                            |         |               |                                      |
     StayDaysPE   StayDaysPE     StayDaysPE                StayDaysPE  StayDaysPE      StayDaysPE                                 |
          |            |            |                            |         |               |             -<<--AgeUpdateEvent------+
          |            |            |                            |         |               |
         -+  -- -- -- -+ -- -- -- --|--  --  [StayDaysEvent]  -- | --  -- -+ -- -- -- -- --|-- --                                 |
          |            |            |                            |         |               |
          +------------+------------+----------------+-----------+---------+---------------+-
                                                     |
                                                     |
                                                     |
                                                     |
                                              JoinAndPrintPE

</pre>

* 由SignalingAdapter不断的将信令发送到Stream:Signaling
* 两种(分别统计白天停留时间和晚上停留时间）StayHoursPE接收信令事件，对于每种统计，每个imsi创建一个StayHoursPE实例
* 由于对于每个StayHoursPE实例，只负责统计一个用户的相关信令，因此在信令处理时加不加锁都对全局性能影响不大
* 整个处理过程忽略系统时间，时间完全以信令的时间为依据
* 每次时间更新（信令到达）时，需要通知所有的StayHoursPE更新时间

# 时间问题

* 为了测试方便和支持回放，时间以信令中的时间为准，跟系统时间无关。
* StayHoursPE需要在每次时间更新时，都检查所有未离开的用户，确定其停留时间是否已经满足要求。
* StayDaysPE需要在每次统计周期变更时，重新统计。（原先为1-10号，可能变成了2-11号）
* JoinAndPrintPE和时间无关。

因此：
* 有两种时间时间：
    * TimeUpdateEvent
        * 每个StayHoursPE,每接收到一个信令，都向所有StayHoursPE发出一个TimeUpdateEvent
    * AgeUpdateEvent
        * StayHoursPE在接受到信令时，不需要考虑是否需要发出AgeUpdate   
        * StayHoursPE在接受到TimeUpdateEvent信令时，检查所有未离开用户。如果Age变更了，则发出AgeUpdateEvent
        * StayDaysPE接受到AgeUpdateEvent后，判断当前用户是否还符合条件，如果原先符合条件，本次不符合条件，则发出不符合条件的时间

# 窗口问题

* 支持5分钟以内的乱序
* 窗口至少保持5分钟以内的信令
* 为了保持简单：在收到TimeUpdateEvent时移动窗口，在收到信令时不移动窗口。
 * 如果收到的信令要求窗口移动，说明信令时最新的信令，此时不移动窗口不影响结果。
* 在收到之前的信令时，重新计算停留时间(窗口之前的停留时间+窗口内的停留时间）。
* 在收到的信令早于窗口最早时间时，丢弃该信令。

# 窗口跨统计周期问题

* 当时间在统计结束时间+窗口时间以内时，只统计上一个统计周期内的停留时间。 即当统计08:00~18:00时，在17号18:05~18号18:05之内，都只记录17号的状态。

