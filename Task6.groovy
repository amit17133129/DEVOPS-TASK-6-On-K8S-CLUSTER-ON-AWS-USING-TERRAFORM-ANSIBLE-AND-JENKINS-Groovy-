freeStyleJob('job1') {
    description("Pull Code from Github")
    scm {
        github('amit17133129/task_2', 'master')
    }
    triggers {
        scm("* * * * *") 
    }
    steps {
        shell('''if sudo ls / | grep devops-task6
then
    sudo rm -rvf /devops-task6
fi
sudo mkdir /devops-task6
sudo cp -rvf * /devops-task6''')
    }
}

freeStyleJob('job2') {        
    triggers {
        upstream('job1')
    }
    steps {
        shell('''if ls /devops-task6 | grep .html
then 
    if sudo kubectl get all | grep myweb
    then
        kubectl delete all --all
        kubectl delete pvc --all
    fi
    sudo kubectl create -f /root/deployment/html_deployment.yml 
    Pod_Name=$(kubectl get pod -l app=myweb -o jsonpath="{.items[0].metadata.name}")
    sleep 50
    sudo kubectl cp /devops-task6/*.html $Pod_Name:/usr/local/apache2/htdocs/
    
elif ls /devops-task6 | grep .php
then 
    if sudo kubectl get all | grep myweb-php
    then
         sudo kubectl delete all --all
         sudo kubectl delete pvc --all
    fi
    sudo kubectl create -f /root/deployment/php_deployment.yml 
    Pod_Name=$(kubectl get pod -l app=myweb -o jsonpath="{.items[0].metadata.name}")
    sleep 50
    sudo kubectl cp /devops-task6/*.php $Pod_Name:/var/www/html/
else
   echo "There is something wrong"
fi
 ''')
    }   
}

freeStyleJob('job3') {
    triggers {
        upstream('job2')
    }
    steps {
        shell('''status=$(sudo curl -o /dev/null -s -w "%{http_code}" 15.207.102.20:30100)
if [[ $status == 200 ]]
then 
   echo "ok"
else 
   echo "There is some error"
   sudo curl --user "<User_name>:<Password>" http://172.17.0.2:8090/job/job4/build?token=Task6
fi
 ''')
    }
}

freeStyleJob('job4') {
    authenticationToken('Task6')
    triggers {
        upstream('job3')
    }
    steps {
        shell('''if sudo kubectl get all | grep myweb
then
    echo "everything is working fine"
else
    python3  /Alert_mail.py
fi''')
    }
}


buildPipelineView('Devops-task6') {
    filterBuildQueue()
    filterExecutors()
    title('CI Pipeline')
    selectedJob('job1')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(10)
}
