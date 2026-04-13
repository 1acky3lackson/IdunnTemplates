import org.mindrot.jbcrypt.BCrypt;
public class test_bcrypt {
    public static void main(String[] args) {
        String hashed = BCrypt.hashpw("password", BCrypt.gensalt());
        System.out.println(BCrypt.checkpw("password", hashed));
    }
}
