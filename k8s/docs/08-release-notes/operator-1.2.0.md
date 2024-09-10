# TigerGraph Operator 1.2.0 Release notes

## Overview

**TigerGraph Operator 1.2.0** is now available, designed to work seamlessly with **TigerGraph version 4.1.0**.

TigerGraph Operator 1.2.0 introduces several significant new features, including the ability to create services for sidecar containers, support for cluster storage resizing, and enhanced Multi-AZ cluster resiliency.

> [!IMPORTANT]
> TigerGraph Operator has had a breaking change since version 1.0.0. If you are still using a version older than 1.0.0, it is strongly recommended that you upgrade to version 1.2.0. Versions older than 1.0.0 have been deprecated.

### kubectl plugin installation

To install the kubectl plugin for TigerGraph Operator 1.2.0, execute the following command:

```bash
curl https://dl.tigergraph.com/k8s/1.2.0/kubectl-tg  -o kubectl-tg
sudo install kubectl-tg /usr/local/bin/
```

### TigerGraph Operator upgrading

#### Upgrading from TigerGraph Operator 1.0.0 and later versions to version 1.2.0

There are no breaking changes in the TigerGraph CRDs for version 1.2.0 if you have installed TigerGraph Operator version 1.0.0 or above. If you have an older version installed, you can upgrade the TigerGraph Operator by following these steps.

To upgrade the TigerGraph CRDs to version 1.2.0, execute the following command:

```bash
kubectl apply -f https://dl.tigergraph.com/k8s/1.2.0/tg-operator-crd.yaml
```

> [!IMPORTANT]
> Please ensure that you have installed the `kubectl-tg` version 1.2.0 before upgrading TigerGraph Operator to version 1.2.0.

Ensure you have installed the correct version of kubectl-tg:

```bash
kubectl tg version

Version: 1.2.0
Default version of TigerGraph cluster: 4.1.0
```

Upgrade TigerGraph Operator using kubectl-tg plugin:

```bash
kubectl tg upgrade --namespace ${YOUR_NAMESPACE_OF_OPERATOR} --operator-version 1.2.0
```

#### Upgrading from TigerGraph Operator versions prior to 1.0.0 to version 1.0.0 and above

This TigerGraph Operator version upgrade introduces breaking changes if you are upgrading from TigerGraph Operator versions prior to 1.0.0. You need to upgrade the TigerGraph Operator, CRD, and the TigerGraph cluster following specific steps.

Refer to the documentation [How to upgrade TigerGraph Kubernetes Operator](../04-manage/operator-upgrade.md) for details.

## New features

- Support lifecycle hooks preDeleteAction and prePauseAction for TigerGraph CR [TP-4706](https://graphsql.atlassian.net/browse/TP-4706)

- Support expanding PVCs of TigerGraph CR automatically [TP-4853](https://graphsql.atlassian.net/browse/TP-4853)

- Support customizing MaxConcurrentReconciles of controllers in K8s operator [TP-5196](https://graphsql.atlassian.net/browse/TP-5196)

- Support the creation of services for sidecar containers [TP-4900](https://graphsql.atlassian.net/browse/TP-4900)

- Support configuring sidecar service in kubectl-tg [TP-5067](https://graphsql.atlassian.net/browse/TP-5067)

- Support Multi-AZ cluster resiliency for better high availability and efficient resource utilization [TP-4854](https://graphsql.atlassian.net/browse/TP-4854)

- Support Configuring topologySpreadConstraints and region awareness in kubectl-tg  [TP-5272](https://graphsql.atlassian.net/browse/TP-5272)

- Support debugging mode in operator [TP-4884](https://graphsql.atlassian.net/browse/TP-4884)

## Improvements

- Support controlling retry behavior of TigerGraphBackup/ TigerGraphRestore [TP-4736](https://graphsql.atlassian.net/browse/TP-4736)

- Record the actual tag of the backup package in TigerGraphBackup.Status and support deleting backup package when deleting TigerGraphBackup CR [TP-4738](https://graphsql.atlassian.net/browse/TP-4738)

- Remove TigerGraphBackupSchedule’s dependence on K8s Cronjob [TP-4809](https://graphsql.atlassian.net/browse/TP-4809)

- Add a new status NotReady to check if the services of TG are Online. [TP-4890](https://graphsql.atlassian.net/browse/TP-4890)

- Validate the format of the backup tag in webhook [TP-5374](https://graphsql.atlassian.net/browse/TP-5374)

- Improve the config update process to avoid restarting all services [TP-4820](https://graphsql.atlassian.net/browse/TP-4820)

- Support setting the ExternalTrafficPolicy of external services to local or cluster based on the TG version in the operator [TP-5425](https://graphsql.atlassian.net/browse/TP-5425)

## Bug Fixes

- Support values in JSON array format in field `tigergraph.spec.tigergraphConfig` [TP-5359](https://graphsql.atlassian.net/browse/TP-5359)

- GSE slow shutdown [CORE-3372](https://graphsql.atlassian.net/browse/CORE-3372)  