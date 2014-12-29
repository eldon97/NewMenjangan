# New Menjangan #

## Installation Guide ##

1. Prepare one user and add to `sudo` group. Then login as that user.
2. Install Java 7 (Ubuntu/Debian: `sudo apt-get install openjdk-7-jre`, CentOS: `sudo yum install java-1.7.0-openjdk`
3. Install Git (Ubuntu/Debian: `sudo apt-get install git`, CentOS: `sudo yum install git`)
4. Clone repository (`git clone https://bitbucket.org/pascalalfadian/newmenjangan.git`)
5. Copy `newmjnserve` init file from `dist/etc/init.d` to `/etc/init.d` (`sudo cp ~/newmenjangan/dist/etc/init.d/newmjnserve /etc/init.d`)
6. Update `/etc/init.d/newmjnserve`: edit `export NEWMJNSERVE_HOME=/vagrant/NewMenjangan` into your environment e.g.: `export NEWMJNSERVE_HOME=/home/pascal/newmenjangan/dist`
7. Enable execute permission for this file (`chmod 755 /etc/init.d/newmjnserve`)
8. Register init script:
    * In Debian/Ubuntu: `sudo /sbin/insserv /etc/init.d/newmjnserve` or `sudo /usr/lib/insserv/insserv /etc/init.d/newmjnserve`
    * In CentOS: `sudo chkconfig --add newmjnserve
9. You may want to start the service immediately (`/etc/init.d/newmjnserve start`)

## MySQL Server Installation ##

Reference from http://azure.microsoft.com/blog/2014/09/02/create-your-own-dedicated-mysql-server-for-your-azure-websites/

1. Install Server `sudo apt-get install mysql-server mysql-client`
2. Check service `sudo service mysql status`
3. Open port 3306 `sudo iptables -A INPUT -i eth0 -p tcp -m tcp --dport 3306 -j ACCEPT`
4. Check port `sudo netstat -anltp|grep :3306`
5. Create SSH tunnel `sudo ssh -fNg -L 3307:127.0.0.1:3306 pascal@newmenjangan.cloudapp.net`
6. Create an endpoint for the port 3307 in the dashboard of the VM in Azure
7. Connect to mysql `mysql -u root -p`
    * Create user `CREATE USER 'tirtayasa'@'%' IDENTIFIED BY '***';`
    * Create database `CREATE DATABASE tirtayasa`
    * Grant privileges `GRANT ALL PRIVILEGES ON tirtayasa.* TO 'tirtayasa'@'%';`
    * Flush privileges `FLUSH PRIVILEGES;`
8. Fill tables using phpmyadmin