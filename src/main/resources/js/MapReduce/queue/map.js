function() {
	emit({year: this.timestamp.getFullYear(), month: this.timestamp.getMonth(), day: this.timestamp.getDate()}, 
		{projects: this.numberOfProjetcs, builds: this.numberOfBuilds, slaves: this.numberOfSlaves, offlineSlaves: this.numberOfOfflineSlaves, idleSlaves: this.numberOfIdleSlaves});
}