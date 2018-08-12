@echo off
echo /v/scape
java -Xmx256m -Xss2m -Dsun.java2d.noddraw=true -XX:CompileThreshold=1500 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Dfile.encoding=ISO-8859-1 -cp ../out/production/Client; com.runescape.Client
pause