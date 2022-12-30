#!/bin/sh
java -cp /app/pgpcloudbackup.jar de.hechler.encrypt.cloudbackup.PGPCloudRestoreMain "$@"
