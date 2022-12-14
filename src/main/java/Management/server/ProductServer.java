package Management.server;


import Management.Product;
import Management.db.ProductRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductServer {
    private ServerSocket serverSocket;
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);
    private List<Product> products = Collections.synchronizedList(new ArrayList<>());
    private int sequence;
    private Connection conn = null;
    private ProductRepository db;

    public static void main(String[] args) {
        ProductServer productServer = new ProductServer();
        try {
            productServer.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            productServer.stop();
        }
    }
    //메소드: 서버 시작
    public void start() throws IOException {
        serverSocket = new ServerSocket(50001);
        System.out.println("[서버] 시작됨");
        db = new ProductRepository();
        products = db.getProducts();

        Thread thread = new Thread(() -> {
            try {
                while(true) {
                    Socket socket = serverSocket.accept();
                    SocketClient sc = new SocketClient(socket);
                }
            } catch (IOException e) {
            }
        });
        thread.start();
    }

    //메소드: 서버 종료
    public void stop() {
        try {
            serverSocket.close();
            threadPool.shutdownNow();
            System.out.println("[서버] 종료됨");
        } catch (IOException e1) {
        }
    }

    //Server 클래스에 중첩 클래스 선언
    public class SocketClient {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;

        public SocketClient(Socket socket) {
            try {
                this.socket = socket;
                this.dis = new DataInputStream(socket.getInputStream());
                this.dos = new DataOutputStream(socket.getOutputStream());
                receive();
            } catch (IOException e) {
                close();
            }
        }

        public void receive() {
            threadPool.execute(() -> {
                try {
                    while(true) {
                        String receiveJson = dis.readUTF();
                        JSONObject request = new JSONObject(receiveJson);
                        int menu = request.getInt("menu");

                        switch (menu) {
                            case 0:
                                list();
                                break;
                            case 1:
                                create(request);
                                break;
                            case 2:
                                update(request);
                                break;
                            case 3:
                                delete(request);
                                break;
                        }
                    }
                } catch (IOException e) {
                    close();
                }
            });

        }

        public void close() {
            try {
                socket.close();
            } catch(Exception e) {}
        }

        public void list() throws IOException {

            JSONArray data = new JSONArray();
            for(Product p : products) {
                JSONObject product = new JSONObject();
                product.put("no", p.getNo());
                product.put("name", p.getName());
                product.put("price", p.getPrice());
                product.put("stock", p.getStock());
                data.put(product);
            }

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", data);
            dos.writeUTF(response.toString());
            dos.flush();
        }

        public void create(JSONObject request) throws IOException {
            JSONObject data = request.getJSONObject("data");
            Product product = new Product();
            product.setNo(++sequence);
            product.setName(data.getString("name"));
            product.setPrice(data.getInt("price"));
            product.setStock(data.getInt("stock"));
            db.add(product);

            products = db.getProducts();

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", "");
            dos.writeUTF(response.toString());
            dos.flush();
        }

        public void update(JSONObject request) throws IOException {
            JSONObject data = request.getJSONObject("data");
            Product product =new Product();

            product.setName(data.getString("name"));
            product.setPrice(data.getInt("price"));
            product.setStock(data.getInt("stock"));

            db.productsUpdate(product);
            products = db.getProducts();

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", "");
            dos.writeUTF(response.toString());
            dos.flush();
        }

        public void delete(JSONObject request) throws IOException {
            JSONObject data = request.getJSONObject("data");
            int no = data.getInt("no");

            db.productsdelete(no);
            products = db.getProducts();

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", "");
            dos.writeUTF(response.toString());
            dos.flush();
        }
    }
}
