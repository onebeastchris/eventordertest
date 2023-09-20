This is a debug tool that can be used to see in which order Geyser events are fired.
It runs independent of the Geyser API version - it scans through the Geyser jar, specifically, the `org/geysermc/geyser/api/event` path, and subscribes to all events to print a simple message when they are fired.

Note: The `GeyserBedrockPingEvent` is not subscribed to as i like to have clean logs :)
