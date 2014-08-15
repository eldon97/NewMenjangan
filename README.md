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
    * In Debian/Ubuntu: `sudo /sbin/insserv /etc/init.d/mjnserve` or `sudo /usr/lib/insserv/insserv /etc/init.d/mjnserve`
    * In CentOS: TODO
