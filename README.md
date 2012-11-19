#s4digester

## 下载代码

    git clone https://github.com/argszero/s4digester.git

## 打包

    gradle installS4R

### hello

用于简单测试的app
    生成：4digester\hello\build\libs\hello-1.0.s4r

### tourist

用于检测景区游客的app

## S4的特点

1. PE之间的通信比较灵活：当PE在同一个节点时，采用本地队列的方式高效的实现了本地通信；当PE在不同的节点时，提供了TCPEmitter(基于jboss.netty框架）和UDPEmitter两种通信方式可供选择
2. 提供了抽象窗口AbstractSlidingWindowPE的内置支持，但对于复杂的窗口模型，还需要自己实现。
3. 提供了内置的DSL的实现（EDSL），可以通过EDSL方便的Build一个流计算拓扑。但这个DSL的实现还只是针对PE层面上的操作，没有对PE做进一步抽象，无法达到类似CQL的DSL的易用程度。
4. 几乎没有内置PE，需要应用开发者做大量的工作。
5. 没有提供监控UI，管理UI，没有自动IO优化的工具。
6. 可以和Hadoop框架YARN较好的结合。
7. 有些简单的BUG，需要在使用时注意（比如，流的名称不能大于20个字符，否则status命令会报错）


