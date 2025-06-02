import java.io.*;
import java.util.Random;

// gera apenas números separados por vírgula sem quebra de linha
public class GerarDadosRefac {
    public static void main(String[] args) throws IOException {
        String arquivoSaida = "arquivoEntrada.txt";
        int totalNumeros =1_500_000; //ajustar conforme o necessário

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
            Random rand = new Random();

            for (int i = 1; i <= totalNumeros; i++) {
                int numero = rand.nextInt(1500000);
                writer.write(Integer.toString(numero));

                if (i < totalNumeros) {
                    writer.write(", ");

                    if (i % 100000== 0) {
                        System.out.println("Gerados " + i + " números...");
                    }
                }
            }
            System.out.println("Gerados "+totalNumeros+" números");
        System.out.println("Arquivo gerado com sucesso: " + arquivoSaida);
    }
}
}

