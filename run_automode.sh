# This shell script is designed for running automatic mode 
# The configuration, test object file and specifcation object file is set in "automaticMode/VeriLin"
# The generated code locates in "automaticMode/VeriLin/verify"
# The result is shown in "result_auto" in Section 6.3

#sudo chmod 777 -R automaticMode 
cd automaticMode/AutoVeriLin #running experiments
sh test.sh list list_int
cat verify/result > ../../result_auto
cd ../..
