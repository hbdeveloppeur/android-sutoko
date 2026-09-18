APP_ID := fr.purpletear.sutoko
ADB := $(shell command -v adb || echo "$(ANDROID_HOME)/platform-tools/adb")

.PHONY: up-build

up-build:
	./gradlew :app:assembleDebug
	devices=""; \
	for i in 1 2 3 4 5; do \
		devices=$$($(ADB) devices | awk 'NR>1 && $$2=="device" {print $$1}'); \
		if [ -n "$$devices" ]; then \
			break; \
		fi; \
		echo "No device found, retrying ($$i/5)..."; \
		sleep 2; \
	done; \
	if [ -z "$$devices" ]; then \
		echo "No device found" >&2; \
		exit 1; \
	fi; \
	for device in $$devices; do \
		echo "Installing app on $$device"; \
		$(ADB) -s $$device install -r app/build/outputs/apk/debug/app-debug.apk || exit 1; \
		echo "Launching app on $$device"; \
		tries=1; \
		until $(ADB) -s $$device shell am start -n $(APP_ID)/.screens.MainActivity; do \
			tries=$$((tries + 1)); \
			if [ $$tries -gt 5 ]; then \
				echo "Failed to launch app on $$device" >&2; \
				exit 1; \
			fi; \
			echo "Launch failed on $$device, retrying ($$tries/5)..."; \
			sleep 2; \
		done; \
	done