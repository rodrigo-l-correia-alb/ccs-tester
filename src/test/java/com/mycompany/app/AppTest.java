package com.mycompany.app;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.random.Well19937c;

import java.io.FileWriter;
import java.io.IOException;
//import org.apache.commons.math3.distribution.KumaraswamyThreeParaDistribution;
//import org.apache.commons.math3.random.RandomGenerator;
//import org.apache.commons.math3.random.Well19937c;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

/*    // KAPPA3
    public static void main(String[] args) {
        RandomGenerator rng = new Well19937c(); // Cria um gerador de números aleatórios
        double loc = params.get("kappa3").get("loc"); // Obtém o valor de loc
        double scale = params.get("kappa3").get("scale"); // Obtém o valor de scale
        double a = params.get("kappa3").get("a"); // Obtém o valor de a
        int sampleSize = datasetLength; // Obtém o tamanho da amostra

        KumaraswamyThreeParaDistribution dist = new KumaraswamyThreeParaDistribution(rng, a, scale, loc); // Cria uma distribuição Kappa 3 com os parâmetros especificados
        double[] sample = dist.sample(sampleSize); // Gera uma amostra aleatória da distribuição

        // Imprime a amostra gerada
        System.out.print("Amostra gerada: [");
        for (int i = 0; i < sample.length; i++) {
            System.out.print(sample[i]);
            if (i < sample.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("]");
    }
*/

        // CHI2 -> Inactividade entre chamadas de voz
        public static void main(String[] args) {

            RandomGenerator rng = new Well19937c();

            // Dur chamadas
/*            double df = 0.6431301870420387;
            double loc = 0.9999999999999999;
            double scale = 398.14750380544035;
            int datasetLength = 23043;*/


            //Inactividade entre chamadas de voz
/*            double df = 0.45184562536470463;
            double scale = 61605.37162866954;
            double loc = 0.010999999999999998;
            int datasetLength = 22992;*/

            //Inactividade entre sessões de dados
            double df = 0.4399814175248422;
            double loc = 0.9999999999999999;
            double scale = 37555.10868230616;
            //int datasetLength = 27322;
            int datasetLength = 10385;

            // Duração das sessões de Dados
/*            double df = 0.2937179729422593;
            double loc = -7.48828076879191e-23;
            double scale = 157630.68828812783;
//            int datasetLength = 30117;
            int datasetLength = 10385;*/

            ChiSquaredDistribution dist = new ChiSquaredDistribution( rng, df ); // Cria uma distribuição chi-quadrado com os parâmetros especificados


            long[] arraySample = new long[datasetLength];


            for (int i = 0; i < datasetLength; i++) {
                double sample = dist.sample()*scale + loc;
                arraySample[i] = Math.round(sample);
                //System.out.println(sample);
            }

            try {
                FileWriter writer = new FileWriter("amostra_ib_data.csv");
                for (long value : arraySample) {
                    writer.write(Double.toString(value));
                    writer.write("\n");
                }
                writer.close();
                System.out.println("Amostra salva em amostra.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


}
