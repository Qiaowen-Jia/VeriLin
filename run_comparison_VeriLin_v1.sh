# This is the for testing VeriLin's performance on 5 different object
# The result corresponds to VeriLin part of Figure 5 in our submitted paper 

sudo chmod -R 777 toolComparison
cd toolComparison/VeriLin
sh runAll_v1.sh
cat result > ../../result_Figure5_VeriLin
cd ../..
