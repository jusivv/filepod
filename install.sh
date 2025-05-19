set -eu
jar_file=$1
config_zip=$2
# create group & user
echo "Create group filepod ..."
sudo groupadd --system filepod
echo "Create user filepod ..."
sudo useradd --system --create-home --gid filepod --home-dir /var/lib/filepod --shell /usr/sbin/nologin --comment "filepod server" filepod
# make directories
echo "Create directories ..."
sudo umask 022
sudo mkdir /etc/filepod
sudo mkdir /var/log/filepod
sudo chown filepod:filepod /var/log/filepod
# create files
echo "Create jar file & configuration files ..."
sudo mv $jar_file /var/lib/filepod/filepod.jar
sudo chown filepod:filepod /var/lib/filepod/filepod.jar
sudo unzip $config_zip -d /etc/filepod
sudo chmod 755 -R /etc/filepod
# create service
echo "Create service ..."
sudo mkdir /etc/systemd/system/filepod.service.d
svc_file=/etc/systemd/system/filepod.service
env_logback=/etc/systemd/system/filepod.service.d/logback.conf
sudo touch $svc_file
sudo echo "[Unit]" >> $svc_file
sudo echo "Description=filepod" >> $svc_file
sudo echo "After=network.target network-online.target" >> $svc_file
sudo echo "Requires=network-online.target" >> $svc_file
sudo echo "[Service]" >> $svc_file
sudo echo "Type=simple" >> $svc_file
sudo echo "Group=filepod" >> $svc_file
sudo echo "User=filepod" >> $svc_file
sudo echo "ExecStart=java -jar /var/lib/filepod/filepod.jar -c /etc/filepod" >> $svc_file
sudo echo "ExecReload=/bin/kill -s HUP $MAINPID" >> $svc_file
sudo echo "ExecStop=/bin/kill -s QUIT $MAINPID" >> $svc_file
sudo echo "[Install]" >> $svc_file
sudo echo "WantedBy=multi-user.target" >> $svc_file
sudo touch $env_logback
sudo echo '[Service]' >> $env_logback
sudo echo 'Environment="LogbackConfigFile=logback-file.xml"' >> $env_logback
sudo echo 'Environment="LOG_HOME=/var/log/filepod"' >> $env_logback
echo "Start Service ..."
sudo systemctl daemon-reload
sudo systemctl enable --now filepod
echo "success !"



