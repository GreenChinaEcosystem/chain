package com.photon.photonchain.extend.compiler;

import com.photon.photonchain.exception.BusinessException;
import com.photon.photonchain.exception.ErrorCode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

/**
 * @Author:Lin
 * @Description:
 * @Date:14:41 2018/7/25
 * @Modified by:
 */
public class MyClassLoader extends ClassLoader {

    public static String compilerContract(String contractStr) {
        StringBuffer bin = new StringBuffer();
        List<String> classNames = Pcompiler.getMember(contractStr, "(public[\\s]+class[\\s]+[^{]*|class[\\s]+[^{]*)");
//        if (classNames.size() > 1) {
//            throw new BusinessException("目前只支持单个类", ErrorCode._10042);
//        }
        //解析类名 start
        String name = null;
        for (int i = 0; i < classNames.size(); i++) {
            String[] c = ArrayUtils.removeAllOccurences(classNames.get(i).split(" "), "");
            if (c[0].equals("public")) {
                name = c[2];
            }
        }
        if (classNames.size() == 1) {
            String[] c = classNames.get(0).split(" ");
            name = c[2];
        }
        if (!checkSingle(contractStr)) {
            throw new BusinessException("目前不支持外部类", ErrorCode._10042);
        }
        //end
        if (StringUtils.isNotBlank(name)) {
            String classurl = string2File(contractStr, name);
            String cmd = "javac.exe -encoding utf-8 " + System.getProperty("user.dir") + File.separator + name + ".java";
            try {
                Runtime.getRuntime().exec(cmd);
                Thread.sleep(1000);
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                // This URL for a directory will be searched *recursively*
//                String file = System.getProperty("user.dir") + File.separator + name + ".class";

                String regex = "^" + (System.getProperty("user.dir") + File.separator).replace("\\", "/") + name + "[\\s\\S]*.class$";

                File readFile = new File(System.getProperty("user.dir") + File.separator);
                List<File> fileList = new ArrayList<>();
                if (readFile.isDirectory()) {
                    File[] files = readFile.listFiles();
                    for (File dirfile : files) {
                        if (test(dirfile, regex)) {
                            fileList.add(dirfile);
                        }
                    }
                }
                for (File f : fileList) {
                    bin.append(Hex.toHexString(new MyClassLoader().getClassByte(f.getAbsolutePath())));
                    if (StringUtils.isNotBlank(bin)) {
                        bin.append(",");
                        f.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException("合约代码有误", ErrorCode._10001);
            } finally {//删除java文件
//                File file0 = new File(System.getProperty("user.dir") + File.separator + name + ".java");
//                file0.delete();
            }
        }
        if(bin.length() > 1){
            bin = bin.delete(bin.length()-1,bin.length());
        }
        return bin.toString();
    }

    public byte[] getClassByte(String file) throws Exception {
        return Files.readAllBytes(new File(file).toPath());
    }

    public static boolean test(File readFile, String regex) {
        String fileName = readFile.getAbsolutePath().replace("\\", "/");
        if (fileName.matches(regex)) {
            return true;
        }
        return false;
    }

    public static String string2File(String res, String name) {
        String file = System.getProperty("user.dir") + File.separator + name + ".java";
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            File distFile = new File(file);
            if (!distFile.getParentFile().exists()) distFile.getParentFile().mkdirs();
            bufferedReader = new BufferedReader(new StringReader(res));
//            bufferedWriter = new BufferedWriter(new FileWriter(distFile));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(distFile),"UTF-8"));
            char buf[] = new char[1024];         //字符缓冲区
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                bufferedWriter.write(buf, 0, len);
            }
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return file;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public Class loadClass() {
        byte[] code = Hex.decode("cafebabe00000034002a0a0008002209000700230800240900070025090007002609000700270700280700290100046e616d650100124c6a6176612f6c616e672f537472696e673b01000673796d626f6c01000469636f6e010008646563696d616c73010001490100063c696e69743e010003282956010004436f646501000f4c696e654e756d6265725461626c6501000967657453796d626f6c01001428294c6a6176612f6c616e672f537472696e673b01000973657453796d626f6c010015284c6a6176612f6c616e672f537472696e673b29560100076765744e616d650100077365744e616d6501005d284c6a6176612f6c616e672f537472696e673b4c6a6176612f6c616e672f537472696e673b4c6a6176612f6c616e672f537472696e673b4c6a6176612f6c616e672f537472696e673b4c6a6176612f6c616e672f537472696e673b295601000767657449636f6e01000773657449636f6e01000b676574446563696d616c7301000328294901000b736574446563696d616c730100042849295601000a536f7572636546696c650100086162632e6a6176610c000f00100c000b000a01000a736466647366736466610c0009000a0c000c000a0c000d000e0100036162630100106a6176612f6c616e672f4f626a6563740021000700080000000400020009000a00000002000b000a00000002000c000a00000002000d000e000000090001000f0010000100110000001d00010001000000052ab70001b100000001001200000006000100000001000100130014000100110000001d00010001000000052ab40002b000000001001200000006000100000008000100150016000100110000002200020002000000062a2bb50002b10000000100120000000a00020000000c0005000d000100170014000100110000001b00010001000000031203b000000001001200000006000100000010000100180019000100110000002200020006000000062a2bb50004b10000000100120000000a000200000014000500150001001a0014000100110000001d00010001000000052ab40005b0000000010012000000060001000000180001001b0016000100110000002200020002000000062a2bb50005b10000000100120000000a00020000001c0005001d0001001c001d000100110000001d00010001000000052ab40006ac000000010012000000060001000000200001001e001f000100110000002200020002000000062a1bb50006b10000000100120000000a0002000000240005002500010020000000020021");
        Class c1 = defineClass(code, 0, code.length);
        return c1;
    }

    public List<Class> binToClass(String bin) {
        String[] bins = bin.split(",");
        List<Class> list = new ArrayList<>();
        if (bins.length == 0) {
            byte[] code = Hex.decode(bin);
            Class c1 = defineClass(code, 0, code.length);
            list.add(c1);
        } else {
            for (String b : bins) {
                byte[] code = Hex.decode(b);
                Class c1 = defineClass(code, 0, code.length);
                list.add(c1);
            }
        }
        return list;

    }

    private static Boolean checkSingle(String str) {
        String temp = getContent(str, "{", "}");
        str = str.replace(temp, "");
        if (str.contains("{") || str.contains("}")) {
            return false;
        }
        return true;
    }

    //获取括号里面内容
    public static String getContent(String str, String head, String end) {
        if (str.indexOf(head) > 0) {
            str = str.substring(str.indexOf(head));
            int he = 0;
            int en = 0;
            int index = 0;
            for (int i = 0; i < str.length(); i++) {
                if (head.equals(String.valueOf(str.charAt(i)))) {
                    he++;
                }
                if (end.equals(String.valueOf(str.charAt(i)))) {
                    en++;
                }
                if (he == en) {
                    index = i;
                    break;
                }
            }
//            str = str.substring(1, index);
            str = str.substring(0, index + 1);
        }
        return str;
    }

    public static void main(String[] args) throws Exception {
        try {
            List<Class> list = new MyClassLoader().binToClass("" +
                    "cafebabe0000003401380a006e009807009908009a08009b08009c08009d08009e08009f0800a00800a10800a20800a30800a40800a50800a60800a70800a80800a90800aa0800ab0800ac0800ad09006b00ae0800af0800b00800b109006b00b20800b30800b40800b50800b60800b709006b00b80700b909006b00ba09006b00bb09006b00bc0700bd0a002600be09006b00bf09006b00c009006b00c109006b00c209006b00c30a006b00c40900c500c60a006b00c70800c80a006b00c90a006b00ca0a006b00cb0a006b00cc0a002600cd0a002600ce0900c500cf0a002600c70700d00a003900980700d10a003b00d20a003900d30a002200be0a003900d40900c500d50a002200d60900c500d70700d80a004300d90800da0a003900db0800dc0a004300980a006b00dd0a00de00d30a00de00db0800df0a002200e00a00e100e20a000200e30a006b00e40a002600e50a006b00e60800e70a000200e80a006b00e90a006b00ea0a002600eb0a000200ec0a000200ed0700ee0a005a00980a005a00ef0a005a00f00a006b00f10800f20800f30a00f400f506405900000000000006bff00000000000000a000200f60a000200f70a00f800f90a00f800fa0700fb0700fc0a006b00980a006b00fd0700fe0a006b00ff0701000100044b4559530100135b4c6a6176612f6c616e672f537472696e673b010007434f4d4d414e440100014d0100046b6579730100165b4c6a617661782f7377696e672f4a427574746f6e3b010008636f6d6d616e64730100016d01000a726573756c74546578740100184c6a617661782f7377696e672f4a546578744669656c643b01000a666972737444696769740100015a010009726573756c744e756d010001440100086f70657261746f720100124c6a6176612f6c616e672f537472696e673b0100106f70657261746556616c6964466c61670100063c696e69743e010003282956010004436f646501000f4c696e654e756d6265725461626c65010004696e697401000d537461636b4d61705461626c650700d001000f616374696f6e506572666f726d656401001f284c6a6176612f6177742f6576656e742f416374696f6e4576656e743b295607009901000f68616e646c654261636b737061636501000c68616e646c654e756d626572010015284c6a6176612f6c616e672f537472696e673b295601000768616e646c654301000e68616e646c654f70657261746f720100116765744e756d62657246726f6d546578740100032829440700fc0700fb01000372756e01000a536f7572636546696c650100114d7943616c63756c61746f722e6a6176610c008200830100106a6176612f6c616e672f537472696e670100013701000138010001390100012f010004737172740100013401000135010001360100012a010001250100013101000132010001330100012d010003312f78010001300100032b2f2d0100012e0100012b0100013d0c007100720100094261636b73706163650100024345010001430c00730072010001200100024d430100024d520100024d530100024d2b0c007400720100136a617661782f7377696e672f4a427574746f6e0c007500760c007700760c007800760100166a617661782f7377696e672f4a546578744669656c640c0082008e0c0079007a0c007b007c0c007d007e0c007f00800c0081007c0c008600830701010c010201030c01040105010009e8aea1e7ae97e599a80c0106008e0c010701080c0109010a0c010b00830c010c010d0c010e010a0c010f01030100126a617661782f7377696e672f4a50616e656c0100136a6176612f6177742f477269644c61796f75740c008201100c011101120c011301140c011501030c011601050c011701030100156a6176612f6177742f426f726465724c61796f75740c008201080100054e6f7274680c0113011801000643656e7465720c0119011a07011b010004576573740c011c011d07011e0c011f01200c012101220c008c00830c0123008e0c008f008301000b303132333435363738392e0c012401250c008d008e0c0090008e0c012601200c012701280c0129012a0100176a6176612f6c616e672f537472696e674275696c6465720c012b012c0c012d01200c00910092010012e999a4e695b0e4b88de883bde4b8bae99bb601000fe99bb6e6b2a1e69c89e58092e695b007012e0c009e012f0c013001310c013001320701330c013001340c0135009201001f6a6176612f6c616e672f4e756d626572466f726d6174457863657074696f6e01000c4d7943616c63756c61746f720c0136010a0100126a617661782f7377696e672f4a4672616d650c0137010d01001d6a6176612f6177742f6576656e742f416374696f6e4c697374656e657201000e6a6176612f6177742f436f6c6f7201000a4c494748545f475241590100104c6a6176612f6177742f436f6c6f723b01000d7365744261636b67726f756e64010013284c6a6176612f6177742f436f6c6f723b29560100087365745469746c6501000b7365744c6f636174696f6e010005284949295601000c736574526573697a61626c65010004285a29560100047061636b010016736574486f72697a6f6e74616c416c69676e6d656e740100042849295601000b7365744564697461626c650100055748495445010007284949494929560100097365744c61796f757401001b284c6a6176612f6177742f4c61796f75744d616e616765723b295601000361646401002a284c6a6176612f6177742f436f6d706f6e656e743b294c6a6176612f6177742f436f6d706f6e656e743b010004626c756501000d736574466f726567726f756e6401000372656401003c284c6a6176612f6c616e672f537472696e673b4c6a6176612f6177742f436f6d706f6e656e743b294c6a6176612f6177742f436f6d706f6e656e743b01000e676574436f6e74656e7450616e6501001628294c6a6176612f6177742f436f6e7461696e65723b0100126a6176612f6177742f436f6e7461696e6572010011616464416374696f6e4c697374656e6572010022284c6a6176612f6177742f6576656e742f416374696f6e4c697374656e65723b295601001a6a6176612f6177742f6576656e742f416374696f6e4576656e74010010676574416374696f6e436f6d6d616e6401001428294c6a6176612f6c616e672f537472696e673b010006657175616c73010015284c6a6176612f6c616e672f4f626a6563743b295a01000773657454657874010007696e6465784f66010015284c6a6176612f6c616e672f537472696e673b2949010007676574546578740100066c656e677468010003282949010009737562737472696e67010016284949294c6a6176612f6c616e672f537472696e673b010006617070656e6401002d284c6a6176612f6c616e672f537472696e673b294c6a6176612f6c616e672f537472696e674275696c6465723b010008746f537472696e6701000e6a6176612f6c616e672f4d6174680100042844294401000776616c75654f66010015284a294c6a6176612f6c616e672f537472696e673b0100152844294c6a6176612f6c616e672f537472696e673b0100106a6176612f6c616e672f446f75626c65010026284c6a6176612f6c616e672f537472696e673b294c6a6176612f6c616e672f446f75626c653b01000b646f75626c6556616c756501000a73657456697369626c6501001873657444656661756c74436c6f73654f7065726174696f6e0021006b006e00010070000b00120071007200000012007300720000001200740072000000020075007600000002007700760000000200780076000000020079007a00000002007b007c00000002007d007e00000002007f0080000000020081007c00000009000100820083000100840000018200050001000001222ab700012a1014bd0002590312035359041204535905120553590612065359071207535908120853591006120953591007120a53591008120b53591009120c5359100a120d5359100b120e5359100c120f5359100d12105359100e12115359100f121253591010121353591011121453591012121553591013121653b500172a06bd0002590312185359041219535905121a53b5001b2a08bd00025903121c535904121d535905121e535906121f535907122053b500212a2ab40017bebd0022b500232a2ab4001bbebd0022b500242a2ab40021bebd0022b500252abb0026591212b70027b500282a04b500292a0eb5002a2a1216b5002b2a04b5002c2ab7002d2ab2002eb6002f2a1230b600312a1101f411012cb600322a04b600332ab60034b10000000100850000004e0013000000270004000b007f000e0096001000b7001200c3001400cf001600db001800e8001b00ed001d00f2001f00f8002100fd00290101002b0108002c010e002e01180030011d003201210033000200860083000100840000034b000700070000022e2ab4002807b600352ab4002803b600362ab40028b20037b60038bb003959b7003a4c2bbb003b5907080606b7003cb6003d033d1c2ab40017bea200332ab400231cbb0022592ab400171c32b7003e532b2ab400231c32b6003f572ab400231c32b20040b60041840201a7ffca2ab400230632b20042b600412ab40023100832b20042b600412ab40023100d32b20042b600412ab40023101232b20042b600412ab40023101332b20042b60041bb003959b7003a4d2cbb003b5904060606b7003cb6003d033e1d2ab4001bbea200332ab400241dbb0022592ab4001b1d32b7003e532c2ab400241d32b6003f572ab400241d32b20042b60041840301a7ffcabb003959b7003a4e2dbb003b5908040606b7003cb6003d03360415042ab40021bea200372ab400251504bb0022592ab40021150432b7003e532d2ab40025150432b6003f572ab40025150432b20042b60041840401a7ffc5bb003959b7003a3a041904bb0043590606b70044b6003d190412452cb6004657190412472bb6004657bb003959b7003a3a051905bb004359b70048b6003d190512472ab40028b60046572ab60049bb0043590608b70044b6004a2ab6004912451905b6004b572ab6004912471904b6004b572ab60049124c2db6004b5703360615062ab40017bea200142ab400231506322ab6004d840601a7ffe803360615062ab4001bbea200142ab400241506322ab6004d840601a7ffe803360615062ab40021bea200142ab400251506322ab6004d840601a7ffe8b1000000020085000000ca00320000003a0008003c0010003e001a00410022004300310044003c0045004f0046005a004700660044006c004a0078004b0085004c0092004d009f004e00ac005100b4005300c3005400ce005500e1005600ec005700f8005400fe005b0106005d0115005e0122005f01370060014300610150005e01560068015f006a016d006b0176006c017f006f018800700194007101a0007401b0007501bc007601c8007701d3007a01e0007b01eb007a01f1007d01fe007e0209007d020f0080021c008102270080022d008300870000003b000cfd003307008801fa0038fd005807008801fa0038fd001907008801fa003dfe007f07008807008801fa001afc000201fa001afc000201fa001a00010089008a00010084000000af000300030000005d2bb6004e4d2c2ab4001b0332b6004f99000a2ab70050a700462c2ab4001b0432b6004f99000f2ab400281212b60051a7002d2c2ab4001b0532b6004f99000a2ab70052a7001912532cb600549b000b2a2cb70055a700082a2cb70056b10000000200850000002e000b0000008b0005008c0012008e0019008f0026009100320092003f009400460095004f00970057009b005c009d00870000000c0005fc001907008b181310040002008c0083000100840000008d00040003000000412ab40028b600574c2bb600583d1c9e00322b031c0464b600594c2bb600589a001a2ab400281212b600512a04b500292a1216b5002ba7000b2ab400282bb60051b10000000200850000002a000a000000a3000800a4000d00a5001100a7001a00a8002100aa002a00ab002f00ac003800af004000b200870000000a0002fd003807008b01070002008d008e00010084000000ba000300020000007b2ab4002999000e2ab400282bb60051a700662b1214b6004f9900352ab40028b600571214b600549c00262ab40028bb005a59b7005b2ab40028b60057b6005c1214b6005cb6005db60051a7002b2b1214b6004f9a00222ab40028bb005a59b7005b2ab40028b60057b6005c2bb6005cb6005db600512a03b50029b1000000020085000000220008000000ba000700bc001200bd002a00bf004d00c0005600c2007500c5007a00c60087000000050003123a270002008f0083000100840000003900020001000000152ab400281212b600512a04b500292a1216b5002bb1000000010085000000120004000000cd000900ce000e00cf001400d000020090008e0001008400000221000500060000016a2ab4002b1206b6004f99002d2ab7005e0e979a00142a03b5002c2ab40028125fb60051a700fe2a59b4002a2ab7005e6fb5002aa700ee2ab4002b1211b6004f99002a2ab4002a0e979a00142a03b5002c2ab400281260b60051a700c82a0f2ab4002a6fb5002aa700bb2ab4002b1215b6004f9900132a59b4002a2ab7005e63b5002aa7009f2ab4002b1210b6004f9900132a59b4002a2ab7005e67b5002aa700832ab4002b120bb6004f9900132a59b4002a2ab7005e6bb5002aa700672ab4002b1207b6004f9900112a2ab4002ab80061b5002aa7004d2ab4002b120cb6004f9900122a2ab4002a1400626fb5002aa700322ab4002b1213b6004f9900122a2ab4002a1400646bb5002aa700172ab4002b1216b6004f99000b2a2ab7005eb5002a2ab4002c9900352ab4002a8f412ab4002a208a67390418040e979a00112ab4002820b80066b60051a700112ab400282ab4002ab80067b600512a2bb5002b2a04b500292a04b5002cb10000000200850000008a0022000000d8000c00db001500dd001a00de002600e0003600e2004200e4004b00e6005000e7005c00e9006900eb007500ed008500ee009100f000a100f100ad00f300bd00f400c900f600d700f700e300f900f200fa00fe00fc010d00fd011900ff0121010101280105012e010601370107013e0108014c010a015a010e015f010f0164011001690111008700000015000d260f250c1b1b1b191a1a13fd002a0403f9000d000200910092000100840000005d00020004000000160e482ab40028b60057b80068b6006948a700044e27af0001000200100013006a00020085000000160005000001190002011b0010011d0013011c0014011e0087000000110002ff001300020700930300010700940000010095008300010084000000370002000200000013bb006b59b7006c4c2b04b6006d2b06b6006fb10000000100850000001200040000012200080123000d01240012012500010096000000020097");
            Method mt = null;
            Class mainClazz = null;
            String regex = "[^$]+";
            for (Class clazz : list) {
                if (clazz.getName().matches(regex)) {//判断主类进入
                    mainClazz = clazz;
                    for (Class c : list) {
                        if (!c.getName().matches(regex)) {//不输入主类load到主类
                            mainClazz.getClassLoader().loadClass(c.getName());//主类
                        }
                    }
                    System.out.println(clazz.getName());
                    Method[] methods = clazz.getDeclaredMethods();
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        System.out.println("属性：" + field.getName() + ":" + field.getType());
                    }
                    for (Method method : methods) {
                        if (method.getName().equals("run")) {
                            Class<?>[] getTypeParameters = method.getParameterTypes();
                            if (getTypeParameters.length == 0) {
                                System.out.println("此方法无参数");
                            }
                            int i = 1, j = 0;
                            for (Class<?> class1 : getTypeParameters) {
                                String parameterName = class1.getName();
                                System.out.println("传入参数类型" + i + "：" + parameterName);
                                i++;
                            }
                            mt = clazz.getMethod("run", getTypeParameters);
                        }
                    }
                }
            }
            System.out.println();
            mt.invoke(mainClazz.newInstance(),null);
        } catch (BusinessException e) {
            e.printStackTrace();
        }

    }
}