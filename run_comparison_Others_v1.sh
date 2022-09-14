
sudo chmod -R 777
cd toolComparison
sh run_baselines.sh subset
cat result.csv > ../result_Figure5_Others
cd ..
