
/**
 * Trabalho Pratico - Redes I Frederico Oliveira e João Victor BATALHA NAVAL
 */
// Classe Cliente

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.Socket;
import java.io.InputStreamReader;

enum TipoNavio {
    PORTA_AVIAO, CONTRATORPEDEIRO, NAVIO_TANQUE, SUBMARINO,
}

enum OrientacaoNavio {
    HORIZONTAL, VERTICAL,
}

class Posicao {
    int linha;
    int coluna;

    Posicao(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }
}

class Navio {
    TipoNavio tipo;
    String orientacao;
    Posicao posicaoInicial;
    int tamanho;

    Navio(TipoNavio tipo, String orientacao, Posicao posicaoInicial) {
        this.tipo = tipo;
        this.orientacao = orientacao;
        this.posicaoInicial = posicaoInicial;
        switch (this.tipo) {
        case PORTA_AVIAO:
            this.tamanho = 5;
            break;
        case CONTRATORPEDEIRO:
            this.tamanho = 4;
            break;
        case NAVIO_TANQUE:
            this.tamanho = 3;
            break;
        case SUBMARINO:
            this.tamanho = 2;
            break;
        default:
            this.tamanho = -1;
            break;
        }
    }
}

class Tabuleiro {
    public int[][] tabuleiroAtual;

    // Codigo de posicao:
    // 0: Agua
    // 1: Barco
    // -1: Barco Danificado
    public Tabuleiro() {
        tabuleiroAtual = new int[10][10];
        for (int i = 0; i < tabuleiroAtual.length; i++) {
            for (int j = 0; j < tabuleiroAtual.length; j++) {
                tabuleiroAtual[i][j] = 0;
            }
        }
    }

    // Coloca os navios de entrada no tabuleiro
    public void colocarNavios(Navio[] navios) {
        for (int i = 0; i < navios.length; i++) {
            for (int j = 0; j < navios[i].tamanho; j++) {
                if (navios[i].orientacao.equals("HORIZONTAL")) {
                    tabuleiroAtual[navios[i].posicaoInicial.linha][navios[i].posicaoInicial.coluna + j] = 1;
                } else {
                    tabuleiroAtual[navios[i].posicaoInicial.linha + j][navios[i].posicaoInicial.coluna] = 1;
                }
            }
        }
    }

    // Metodo que imprime o tabuleiro
    public void printTabuleiro() {
        for (int i = 0; i < tabuleiroAtual.length; i++) {
            for (int j = 0; j < tabuleiroAtual[i].length; j++) {
                System.out.print(tabuleiroAtual[i][j]);
                System.out.print(' ');
            }
            System.out.println();
        }
    }

    // Retorna o objeto Tabuleiro:
    public int[][] getTabuleiro() {
        return tabuleiroAtual;
    }

    public void computarAcerto(int linha, int coluna) {
        tabuleiroAtual[linha][coluna] = -1;
    }

    public boolean checarVitoria() {
        boolean perdeu = true;
        for (int i = 0; i < tabuleiroAtual.length; i++) {
            for (int j = 0; j < tabuleiroAtual[i].length; j++) {
                if (tabuleiroAtual[i][j] == 1) {
                    perdeu = false;
                    return perdeu;
                }
            }
        }
        return perdeu;
    }
}

public class Client {

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        // Modelo do arquivo de entrada:
        // NUM_BARCO ORIENTACAO POS_INICIAL_X POS_INICIAL Y
        // NUM_BARCO entre 1 e 4
        // ORIENTACAO VERITCAL ou HORIZONTAL
        // POS_INICIAL_X e POS_INICIAL_Y inteiros entre 0 e 9,
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(args[0]));
            String linhaAtual;
            ArrayList<Navio> navios = new ArrayList<Navio>();
            while ((linhaAtual = fileReader.readLine()) != null) {
                String[] parametros = linhaAtual.split(" ");
                TipoNavio tipo = parametros[0].equals("1") ? TipoNavio.PORTA_AVIAO
                        : parametros[0].equals("2") ? TipoNavio.CONTRATORPEDEIRO
                                : parametros[0].equals("3") ? TipoNavio.NAVIO_TANQUE : TipoNavio.SUBMARINO;
                String orientacao = parametros[1];
                Posicao posicaoInicial = new Posicao(Integer.parseInt(parametros[2]), Integer.parseInt(parametros[3]));
                navios.add(new Navio(tipo, orientacao, posicaoInicial));

            }

            // Cria o tabuleiro e posiciona os navios e o imprime na tela:
            Tabuleiro tabuleiro = new Tabuleiro();
            tabuleiro.colocarNavios(navios.toArray(new Navio[navios.size()]));
            tabuleiro.printTabuleiro();

            // Cria as conexoes com o servidor:
            String ip = "127.0.0.1";
            Socket socketCliente = new Socket(ip, 6789);

            BufferedReader inputServidor = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

            DataOutputStream saidaServidor = new DataOutputStream(socketCliente.getOutputStream());

            boolean fimJogo = false;

            while (!fimJogo) {
                // Cliente manda uma posicao para tentar acertar um dos navios do servidor:
                String jogadaCliente = scan.nextLine();
                saidaServidor.writeBytes(jogadaCliente + '\n');
                saidaServidor.flush();

                // Cliente recebe uma posicao do servidor:
                String resultadoServidor = inputServidor.readLine();
                System.out.println("Servidor disse: Voce " + resultadoServidor);

                String jogadaServidor = inputServidor.readLine();
                int linhaHitServ, colunaHitServ;
                linhaHitServ = Integer.parseInt(jogadaServidor.split(" ")[0]);
                colunaHitServ = Integer.parseInt(jogadaServidor.split(" ")[1]);

                if (tabuleiro.getTabuleiro()[linhaHitServ][colunaHitServ] == 1) {
                    tabuleiro.computarAcerto(linhaHitServ, colunaHitServ);
                    saidaServidor.writeBytes("acertou" + '\n');
                    saidaServidor.flush();
                } else {
                    saidaServidor.writeBytes("errou" + '\n');
                    saidaServidor.flush();
                }

                if (tabuleiro.checarVitoria()) {
                    fimJogo = true;
                    System.out.println("Voce perdeu.");
                    saidaServidor.writeBytes("Voce venceu!" + '\n');
                    saidaServidor.flush();
                }

                tabuleiro.printTabuleiro();
            }

            // Fechar leitores de entrada
            try {
                socketCliente.close();
                scan.close();
                fileReader.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}