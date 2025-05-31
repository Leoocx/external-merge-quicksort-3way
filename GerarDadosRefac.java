import java.io.*;
import java.util.Random;

// gera apenas números separados por vírgula sem quebra de linha
public class GerarDadosRefac {
    public static void main(String[] args) throws IOException {
        String arquivoSaida = "arquivoEntrada.txt";
        int totalNumeros = 5_000_000; //ajuste conforme o necessário

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
            Random rand = new Random();

            for (int i = 1; i <= totalNumeros; i++) {
                int numero = rand.nextInt(5000000);
                writer.write(Integer.toString(numero));

                if (i < totalNumeros) {
                    writer.write(", ");

                    if (i % 100_000 == 0) {
                        System.out.println("Gerados " + i + " números...");
                    }
                }
            }
        System.out.println("Gerados 5000000 números...");
        System.out.println("Arquivo gerado com sucesso: " + arquivoSaida);
    }
}
}

