
sudo chmod -R 777 toolComparison
cd toolComparison
sh run_baselines.sh
cat result.csv > ../result_Figure4_Others
cd ..
