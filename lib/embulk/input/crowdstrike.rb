Embulk::JavaPlugin.register_input(
  "crowdstrike", "org.embulk.input.crowdstrike.CrowdstrikeFileInputPlugin",
  File.expand_path('../../../../classpath', __FILE__))
