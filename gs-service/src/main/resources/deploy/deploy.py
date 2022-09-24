import json
import sys
import boto3
import os
import zipfile
from os import path
import subprocess
from botocore.exceptions import ClientError

buildWarFileName = 'gs-service.war'
dockerWarFileName = 'gs-service.war'

gsServiceDockerImageRepositry = '672812193635.dkr.ecr.us-east-1.amazonaws.com/gs-service'

awsStackNameSuffix = 'Stack'


def cleanDeployDir():
    deployFiles = os.listdir('.')

    for file in deployFiles:

        if "deploy.py" not in file:
            if "deployConfigurations.json" not in file:
                if not path.isdir(file):
                    print('     Deleting: ' + file)
                    os.remove(file)

    print('     Deleting: ' + '../docker/' + dockerWarFileName)
    os.remove('../docker/' + dockerWarFileName)


def createEngineImage(number):
    
    login = subprocess.check_output(["aws", "ecr", "get-login", "--no-include-email", "--region", "us-east-1"])

    print(login)

    replaced = str(login).replace('b\'', '').replace('\\n\'', '').replace('\\r', '')

    subprocess.call(replaced.split(' '))
    
    print('     Copying: ' + buildWarFileName + ' to ../docker/' + dockerWarFileName)

    subprocess.call(["cp", '../../../../target/' + buildWarFileName, '../docker/' + dockerWarFileName])

    imageName = gsServiceDockerImageRepositry + ':build-' + number

    print('     Created docker image: ' + imageName)

    print('     Pushing docker image: ' + imageName)

    subprocess.call(["docker", 'build', '-t', imageName, '../docker/'])

    subprocess.call(["docker", 'push', imageName])

    return imageName


def loadConfigurations():
    f = open("deployConfigurations.json", "r")
    configurations = json.loads(f.read())

    return configurations


def loadClusterConfigurations(confFile):
    f = open(confFile, "r")
    configurations = json.loads(f.read())

    return configurations


def updateClusterConf(cloudName, clusterName, host, clusterConfigurationFile, image):
    if 'AWS' == cloudName:

        hostedzoneid = ''

        route53 = boto3.client('route53')

        zones = route53.list_hosted_zones()['HostedZones']

        for zone in zones:
            if zone['Name'] in host + '.':
                hostedzoneid = zone['Id'].split('/')[len(zone['Id'].split('/')) - 1]

        if hostedzoneid == '':
            raise Exception('Unable to find hosted zone id for host ' + host)

        clusterParams = loadClusterConfigurations(clusterConfigurationFile)

        clusterParams['Parameters']['EcsClusterName']['Default'] = clusterName

        clusterParams['Parameters']['EnvironmentHost']['Default'] = host

        clusterParams['Parameters']['HostedZoneId']['Default'] = hostedzoneid

        clusterParams['Parameters']['GSServiceImage']['Default'] = image

        print('     Dynamic Stack Parameters:')
        print('          - EcsClusterName: ' + clusterName)
        print('          - EnvironmentHost: ' + host)
        print('          - HostedZoneId: ' + hostedzoneid)
        print('          - GSServiceImage: ' + image)

        stackName = clusterName + awsStackNameSuffix

        client = boto3.client('cloudformation')

        response = client.list_stacks()

        stacks = response['StackSummaries']

        for stack in stacks:

            if stackName in stack['StackName']:

                if 'DELETE_COMPLETE' in stack['StackStatus']:
                    print('     Found DELETED Stack ' + stackName)
                    continue

                print('     Stack ' + stackName + ' already exists, updating')

                try:
                    response = client.update_stack(
                        StackName=stackName,
                        TemplateBody=json.dumps(clusterParams))

                except ClientError as e:
                    print('     Exception updating AWS Stack ' + str(e))

                    if 'No updates are to be performed.' in e.response['Error']['Message']:
                        print('     No required updates exception detected, returning with no exception')
                    else:
                        raise Exception(e.response['Error']['Message'])

                return

        print('     Stack ' + stackName + ' does not exist, creating')

        response = client.create_stack(
            StackName=stackName,
            TemplateBody=json.dumps(clusterParams))

    return


################################################################################################################
# MAIN
################################################################################################################

gitstatus = subprocess.check_output(["git", "status"]).strip().decode("utf-8")

cleanworkingdirectory = False

for line in gitstatus.splitlines():
    if 'nothing to commit, working directory clean' in line:
        cleanworkingdirectory = True
    if 'nothing to commit, working tree clean' in line:
        cleanworkingdirectory = True


if cleanworkingdirectory:
    if len(sys.argv) != 4:
        print("usage: %s <environment> <cloud> <configURL>" % sys.argv[0])
        sys.exit(1)

    commit = subprocess.check_output(["git", "rev-parse", "HEAD"]).strip().decode("utf-8")
    buildNumber = commit
    environmentName = sys.argv[1]
    cloudName = sys.argv[2]
    confURL = sys.argv[3]

    print('MAIN --> Loading configurations')
    confs = loadConfigurations()



    print('MAIN --> Building ' + buildNumber)
    subprocess.run("mvn -f ../../../../pom.xml -Dconfiguration.url=" + confURL + " clean package", shell=True)

    print('MAIN --> Starting deploy of build ' + buildNumber + ' to environment ' + environmentName)


    print('MAIN --> Create/Push Engine Image')
    image = createEngineImage(buildNumber)

    print('MAIN --> Update Cluster Configuration')
    updateClusterConf(cloudName, confs[environmentName]['clusterName'], confs[environmentName]['host'], confs[environmentName][cloudName]['clusterConfigurationFile'], image)

    print('MAIN --> Clean deploy dir')
    cleanDeployDir()

else:
    print('Commit your changes before deploying')
