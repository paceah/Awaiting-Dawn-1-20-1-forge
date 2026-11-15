package com.paceah.awaitingdawn;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Generates the editable distribution config from the same specification used by Forge */
public final class PrepareRunConfig {
    public static void main(String[] args) throws Exception {
        Path run=Path.of(args[0]);
        Path editable=run.resolve("config/awaitingdawn-server.toml");
        Path defaults=run.resolve("defaultconfigs/awaitingdawn-server.toml");
        Files.createDirectories(editable.getParent());
        Files.createDirectories(defaults.getParent());
        // Upgrade missing options and comments without resetting valid user settings
        // Parse errors fail the build rather than silently replacing a manually edited file
        try(var config=CommentedFileConfig.builder(editable).sync().build()) {
            if(Files.exists(editable)) config.load();
            AwaitingDawnServerConfig.SPEC.correct(config);
            config.save();
        }
        // Forge SERVER configs are per-world; this template is used when creating new worlds
        Files.copy(editable,defaults,StandardCopyOption.REPLACE_EXISTING);
    }
}
