package Management.client;

import Management.Product;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ProductClient {

    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    Scanner scanner = new Scanner(System.in);

    public void connect() throws IOException {

        socket = new Socket("localhost", 50001);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        System.out.println("[클라이언트] 서버에 연결됨");

        list();
    }

    public void menu() throws IOException {
        System.out.println("");
        System.out.println("----------------------------------------------------------");
        System.out.println("메뉴: 1.Create | 2.Update | 3.Delete | 4.Exit");
        System.out.print("선택: ");
        String menuNo = scanner.nextLine();

        switch (menuNo) {
            case "1":
                create();
                break;
            case "2":
                update();
                break;
            case "3":
                delete();
                break;
            case "4":
                exit();
                break;
        }

    }

    public void list() throws IOException {

        System.out.println("");
        System.out.println("[상품 목록]");
        System.out.println("----------------------------------------------------------");
        System.out.printf("%-6s%-20s%-15s%-6s\n", "no", "name", "price", "stock");
        System.out.println("----------------------------------------------------------");
        //요청하기
        JSONObject request = new JSONObject();
        request.put("menu", 0);
        request.put("data", new JSONObject());
        dos.writeUTF(request.toString());
        dos.flush();
        //응답받기
        JSONObject response = new JSONObject(dis.readUTF());
        if(response.getString("status").equals("success")) {
            JSONArray data = response.getJSONArray("data");
            for(int i = 0; i < data.length(); i++) {
                JSONObject product = data.getJSONObject(i);
                System.out.printf("%-6s%-20s%-15s%-6s\n",
                        product.getInt("no"), product.getString("name"),
                        product.getInt("price"), product.getInt("stock"));
            }
        }
        menu();
    }

    public void create() throws IOException {

        System.out.println("[상품 생성]");
        Product product = new Product();
        System.out.print("상품 이름: ");
        String name = scanner.nextLine();
        product.setName(name);
        System.out.print("상품 가격: ");
        int price = Integer.parseInt(scanner.nextLine());
        product.setPrice(price);
        System.out.print("상품 재고: ");
        int stock = Integer.parseInt(scanner.nextLine());
        product.setStock(stock);

        //Json 객체 속성 추가
        JSONObject data = new JSONObject();
        data.put("name", product.getName());
        data.put("price", product.getPrice());
        data.put("stock", product.getStock());
        //요청하기
        JSONObject request = new JSONObject();
        request.put("menu", 1);
        request.put("data", data);
        dos.writeUTF(request.toString());
        dos.flush();
        //응답받기
        JSONObject response = new JSONObject(dis.readUTF());
        if(response.getString("status").equals("success")) {
            list();
        }
    }

    public void update() throws IOException {

        System.out.println("[상품 수정]");
        Product product = new Product();
        System.out.print("상품 번호: ");
        int no = Integer.parseInt(scanner.nextLine());
        product.setNo(no);
        System.out.print("이름 변경: ");
        String name = scanner.nextLine();
        product.setName(name);
        System.out.print("가격 변경: ");
        int price = Integer.parseInt(scanner.nextLine());
        product.setPrice(price);
        System.out.print("재고 변경: ");
        int stock = Integer.parseInt(scanner.nextLine());
        product.setStock(stock);

        JSONObject data = new JSONObject();
        data.put("no", product.getNo());
        data.put("name", product.getName());
        data.put("price", product.getPrice());
        data.put("stock", product.getStock());

        JSONObject request = new JSONObject();
        request.put("menu", 2);
        request.put("data", data);
        dos.writeUTF(request.toString());
        dos.flush();

        JSONObject response = new JSONObject(dis.readUTF());
        if (response.getString("status").equals("success")) {
            list();
        }
    }

    public void delete() throws IOException {

        System.out.println("[상품 삭제]");
        System.out.print("상품 번호: ");
        int no = Integer.parseInt(scanner.nextLine());

        JSONObject data = new JSONObject();
        data.put("no", no);

        JSONObject request = new JSONObject();
        request.put("menu", 3);
        request.put("data", data);
        dos.writeUTF(request.toString());
        dos.flush();

        JSONObject response = new JSONObject(dis.readUTF());
        if (response.getString("status").equals("success")) {
            list();
        }
    }

    public void exit() {
        try {
            socket.close();
            scanner.close();
        } catch (Exception e) {}
        System.out.println("[클라이언트] 프로그램 종료");
    }

    public static void main(String[] args) {
        try {
            ProductClient productClient = new ProductClient();
            productClient.connect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
