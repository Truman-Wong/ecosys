# Create TigerGraph cluster with access to S3

Since TigerGraph Operator 1.2.0 and TigerGraph 4.1.0, we support using RoleARN instead of aws access key when backup/restore the cluster.
To use RoleARN in TigerGraphBackup/TigerGraphBackupSchedule/TigerGraphRestore, you must ensure that aws-cli in TigerGraph Pods can access S3 bucket
with the RoleARN specified in TigerGraphBackup/TigerGraphBackupSchedule/TigerGraphRestore CR.

There are many different ways to achieve this, in this document, we will show two simple ways to achieve this.

## Use AWS Service Account(Only work on EKS)

### Step1: Create a service account and give it proper role

1. Create a role that can access S3 Bucket named accessS3Role. Please refer to [this document](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/AuroraMySQL.Integrating.Authorizing.IAM.CreateRole.html)

2. Create a policy (The Statement should guarantee that the service account which attaches the policy can access S3)

    ```bash
    aws iam create-policy --policy-name AssumeAccessS3RolePolicy --policy-document '{
        "Version": "2012-10-17",
        "Statement": [
            {
            "Effect": "Allow",
            "Action": "sts:AssumeRole",
            "Resource": "arn:aws:iam::123456789:role/accessS3Role"
            }
        ]
    }'
    ```

3. Create a service account and attach to the policy

    ```bash
    eksctl create iamserviceaccount --name tg-service-account --namespace tigergraph --cluster your-eks-cluster --role-name tg-role \
      --attach-policy-arn arn:aws:iam::123456789:policy/AssumeAccessS3RolePolicy  --approve
    ```

### Step2: Create a ConfigMap for aws config

The path of the `web_identity_token_file` is a fixed path, the file is generated by ELS. The role must be the role that we created for the service account.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: aws-config
  namespace: tigergraph
data:
  aws_config: |
    [default]
    role_arn= arn:aws:iam::123456789:role/tg-role
    web_identity_token_file=/var/run/secrets/eks.amazonaws.com/serviceaccount/token
    region=us-west-2
```

### Step3: Create a cluster and mount the ConfigMap

```yaml
apiVersion: graphdb.tigergraph.com/v1alpha1
kind: TigerGraph
metadata:
  name: test-cluster
  namespace: tigergraph
spec:
  ha: 1
  image: docker.io/tigergraph/tigergraph-k8s:4.1.0
  imagePullPolicy: IfNotPresent
  imagePullSecrets:
    - name: tigergraph-image-pull-secret
  license: ${YOUR_LICENSE}
  listener:
    type: LoadBalancer
  # the service account that we created
  serviceAccountName: tg-service-account
  privateKeyName: ssh-key-secret
  replicas: 1
  resources:
    requests:
      cpu: "6"
      memory: 12Gi
  storage:
    type: persistent-claim
    volumeClaimTemplate:
      accessModes:
        - ReadWriteOnce
      resources:
        requests:
          storage: 20G
      volumeMode: Filesystem
  # specify the custom volume and volume mount
  customVolumes:
      - name: aws-config
        configMap:
          name: aws-config
          items:
            - key: aws_config
              path: config
  customVolumeMounts:
      - name: aws-config
        mountPath: /home/tigergraph/.aws
        readOnly: true
```

Then we can use the RoleARN `arn:aws:iam::123456789:role/accessS3Role` to create backup to S3.

## Configure AWS Access Key for the cluster

### Step1: Create a ConfigMap for aws config and aws credentials

> [!IMPORTANT]
> You need to ensure that the user corresponding to this access key has the permission to assume the role you use in backup/restore CR.

Assume that `YOUR_ACCESS_KEY` has the permission to assume the role `arn:aws:iam::123456789:role/accessS3Role`, create following ConfigMap:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: aws-config
  namespace: tigergraph
data:
  aws_config: |
    [default]
    region=us-west-2
  aws_credentials: |
    [default]
    aws_access_key_id=YOUR_ACCESS_KEY
    aws_secret_access_key=YOUR_SECRET
```

### Step2: Create a cluster and mount the ConfigMap

```yaml
apiVersion: graphdb.tigergraph.com/v1alpha1
kind: TigerGraph
metadata:
  name: test-cluster
  namespace: tigergraph
spec:
  ha: 1
  image: docker.io/tigergraph/tigergraph-k8s:4.1.0
  imagePullPolicy: IfNotPresent
  imagePullSecrets:
    - name: tigergraph-image-pull-secret
  license: ${YOUR_LICENSE}
  listener:
    type: LoadBalancer
  privateKeyName: ssh-key-secret
  replicas: 1
  resources:
    requests:
      cpu: "6"
      memory: 12Gi
  storage:
    type: persistent-claim
    volumeClaimTemplate:
      accessModes:
        - ReadWriteOnce
      resources:
        requests:
          storage: 10G
      volumeMode: Filesystem
  customVolumes:
      - name: aws-config
        configMap:
          name: aws-config
          items:
            - key: aws_config
              path: config
  customVolumeMounts:
      - name: aws-config
        mountPath: /home/tigergraph/.aws
        readOnly: true
```

Then you can use the RoleARN `arn:aws:iam::123456789:role/accessS3Role` to create backup to S3.