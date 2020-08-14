job('Job1') {
    triggers {
        upstream('Admin(Seed)', 'SUCCESS')
    }
    scm{
        github('amit17133129/task_2' , 'master')
    }
    triggers {
        githubPush()
    }
    steps {
        shell('sudo cp -rvf * /root/DevOpsAL_task6/')
    }
}



job('Job2') {
    triggers {
        upstream('Job1', 'SUCCESS')
    }
    steps{
        shell('''
            if sudo ls /root/DevOpsAL_task6/ | grep .html
            then
                if kubectl get deploy | grep html-deploy
                then
                    kubectl delete -f /root/DevOpsAL_task6/html-pvc.yml
                fi
                
                kubectl create -f /root/DevOpsAL_task6/html-pvc.yml
                sleep 3
                pod=$(kubectl get pods -l env=production --output=jsonpath={.items[0]..metadata.name} | grep html-deploy)
                kubectl cp /root/DevOpsAL_task6/*.html $pod:/usr/local/apache2/htdocs/
            fi 
            if sudo ls /root/DevOpsAL_task6/ | grep .php
            then
                if kubectl get deploy | grep php-deploy
                then
                    kubectl delete -f /root/DevOpsAL_task6/php-pvc.yml
                fi
                
                kubectl create -f /root/DevOpsAL_task6/php-pvc.yml
                sleep 3
                pod=$(kubectl get pods -l env=production --output=jsonpath={.items[0]..metadata.name} | grep php-deploy)
                kubectl cp /root/DevOpsAL_task6/*.php $pod:/var/www/html/
            fi
        ''') 
    }
    
}

job('Job3') {
    triggers {
        upstream('Job2', 'SUCCESS')
    }
    steps {
        shell('''
            status=$(curl -o /dev/null -sw "%{http_code}" 15.207.102.20:30001)
            if [[ $status == 200 ]]
            then
                exit 0
            else
                exit 1
            fi
        ''')
    }
    publishers {
        extendedEmail {
            recipientList('amitsharma13318@gmail.com')
            defaultSubject('Job status')
            attachBuildLog(attachBuildLog = true)
            defaultContent('Status Report')
            contentType('text/html')
            triggers {
                failure {
                    subject('build Status')
                    content('Body')
                    sendTo {
                        developers()
                        recipientList()
                    }
	        }
            }
        }
    }
}


buildPipelineView('DevOpsAL_task6') {
    filterBuildQueue()
    filterExecutors()
    title('DevOpsAL_task6')
    displayedBuilds(5)
    selectedJob('Job1')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(60)
}
