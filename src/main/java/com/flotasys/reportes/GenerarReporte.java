package com.flotasys.reportes;

import org.eclipse.birt.report.engine.api.*;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Ejecuta un .rptdesign contra la base real y exporta el resultado a PDF.
 * Uso: java -jar reportes-birt.jar <reporte.rptdesign> <salida.pdf> [mesAnio=pMesAnio:2024-05 ...]
 */
public class GenerarReporte {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: GenerarReporte <reporte.rptdesign> <salida.pdf> [param=valor ...]");
            System.exit(1);
        }
        String rutaDiseno = args[0];
        String rutaSalida = args[1];

        EngineConfig config = new EngineConfig();
        IReportEngine engine = new ReportEngine(config);

        try {
            IReportRunnable diseno = engine.openReportDesign(rutaDiseno);
            IRunAndRenderTask tarea = engine.createRunAndRenderTask(diseno);

            for (int i = 2; i < args.length; i++) {
                String[] par = args[i].split("=", 2);
                if (par.length == 2) {
                    tarea.setParameterValue(par[0], par[1]);
                }
            }

            PDFRenderOption opciones = new PDFRenderOption();
            opciones.setOutputFormat("pdf");
            try (FileOutputStream salida = new FileOutputStream(new File(rutaSalida))) {
                opciones.setOutputStream(salida);
                tarea.setRenderOption(opciones);
                tarea.run();
            }
            tarea.close();
            System.out.println("Reporte generado: " + rutaSalida);
        } finally {
            engine.destroy();
        }
    }
}
