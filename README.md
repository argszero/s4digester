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
* 选取最近10天内满足下面条件的用户
* 每天8:00-18:00在景区停留时长超过3小时天数小于5天
* 每天18:00到次日8:00在景区停留超过5小时小于5天
* 在网时长超过3个月


d:\scm\git\s4digester\s4digester\tourist\build\libs\tourist-1.0.s4r
