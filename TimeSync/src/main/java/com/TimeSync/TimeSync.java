package com.TimeSync;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class TimeSync extends JavaPlugin implements CommandExecutor {

    private static final DateTimeFormatter RFC_1123_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);
    private static final double TICKS_POR_SEGUNDO = 24000.0 / 86400.0;
    private static final String DEFAULT_WORLD = "world";
    private static final String DEFAULT_TIMEZONE = "America/Guayaquil";
    private static final int DEFAULT_INTERVAL = 10;

    private BukkitScheduler scheduler;
    private Logger logger;
    private FileConfiguration config;

    private List<World> worlds;
    private ZoneId zonaHoraria;
    private int taskId = -1;
    private int intervalo;

    @Override
    public void onEnable() {
        scheduler = Bukkit.getScheduler();
        logger = getLogger();
        config = getConfig();

        saveDefaultConfig();
        cargarConfiguracion();

        if (worlds == null || worlds.isEmpty()) {
            logger.severe("¡No hay mundos válidos! Desactivando plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        iniciarTarea();
        logActivacion();
        getCommand("timesync").setExecutor(this);
    }

    private void cargarConfiguracion() {
        try {
        	
            String zona = config.getString("timezone", DEFAULT_TIMEZONE);
            try {
                zonaHoraria = ZoneId.of(zona);
            } catch (DateTimeException e) {
                logger.severe("Zona horaria inválida. Usando: " + DEFAULT_TIMEZONE);
                zonaHoraria = ZoneId.of(DEFAULT_TIMEZONE);
            }

            intervalo = Math.max(config.getInt("update-interval", DEFAULT_INTERVAL), 1) * 20;
            
            List<String> nombresMundos = config.getStringList("worlds");
            worlds = new ArrayList<>();
            for (String nombre : nombresMundos) {
                World mundo = Bukkit.getWorld(nombre);
                if (mundo != null) {
                    worlds.add(mundo);
                } else {
                    logger.warning("Mundo '" + nombre + "' no encontrado.");
                }
            }

            if (worlds.isEmpty()) {
                worlds.add(Bukkit.getWorld(DEFAULT_WORLD));
                logger.info("Usando mundo por defecto: " + DEFAULT_WORLD);
            }

        } catch (Exception e) {
            logger.severe("Error en configuración: " + e.getMessage());
            zonaHoraria = ZoneId.of(DEFAULT_TIMEZONE);
            intervalo = DEFAULT_INTERVAL * 20;
        }
    }

    private void iniciarTarea() {
        taskId = scheduler.scheduleSyncRepeatingTask(
            this,
            this::sincronizarTiempo,
            0L,
            intervalo
        );
    }

    private void sincronizarTiempo() {
        if (worlds.isEmpty()) return;
        LocalTime horaActual = LocalTime.now(zonaHoraria);
        long ticks = calcularTicks(horaActual);
        for (World mundo : worlds) {
            if (mundo.getPlayers().isEmpty()) continue;
            try {
                mundo.setTime(ticks);
            } catch (Exception e) {
                logger.warning("Error en " + mundo.getName() + ": " + e.getMessage());
            }
        }
    }

    private long calcularTicks(LocalTime hora) {
        int segundosDesdeMedianoche = hora.toSecondOfDay();
        double offset = 18000;
        double ticks = (segundosDesdeMedianoche * TICKS_POR_SEGUNDO + offset) % 24000;
        return (long) ticks;
    }

    private void logActivacion() {
        logger.info(() -> String.join("\n",
            "====================================",
            "TimeSync activado correctamente",
            "Zona horaria: " + zonaHoraria,
            "Hora servidor (UTC): " + RFC_1123_FORMATTER.format(Instant.now()),
            "Mundos: " + worlds.stream().map(World::getName).toList(),
            "Intervalo: " + (intervalo / 20) + "s",
            "===================================="
        ));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("time")) {
            LocalTime hora = LocalTime.now(zonaHoraria);
            sender.sendMessage(ChatColor.YELLOW + "Hora real: " + ChatColor.AQUA + 
                hora.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        if (taskId != -1) {
            scheduler.cancelTask(taskId);
            taskId = -1;
        }
        logger.info("Plugin desactivado");
    }
}