import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class Interpreter {

    private String fileName = "";
    private XMLToken currentToken;
    private Document domDocument;
    private Integer tokenCounter = 0;
    private Integer varCounter = 0;
    private Integer addCounter = 0;
    private NodeList childNodes;
    private ArrayList<VarType> varList;
    private ArrayList<AddType> addList;
    private Map<String, Integer> addedValues;
    private String printVar;

    public Interpreter(String xmlFile) {
        fileName = xmlFile;
        try {
            File inputFile = new File(fileName);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            domDocument = builder.parse(inputFile);
            domDocument.getDocumentElement().normalize();
            childNodes = domDocument.getDocumentElement().getChildNodes();
            varList = new ArrayList<VarType>();
            addList = new ArrayList<AddType>();
            addedValues = new HashMap<>();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }

    public XMLToken getNextXMLToken() {
        XMLToken token = new XMLToken();
        if (tokenCounter == 0) {
            String rootElementName =  domDocument.getDocumentElement().getNodeName();
            token = new XMLToken(tokenType.PROGRAM, rootElementName);
            tokenCounter++;
        } else if (tokenCounter > 0) {
            
            Integer noOfNodes = childNodes.getLength();
            if (tokenCounter < noOfNodes) {
                
                Node node = childNodes.item(tokenCounter);
                //System.out.println("tokenCounter: " + tokenCounter);
                if (node.getNodeType() == 1) {
                    
                    switch(node.getNodeName()) {
                        case "var":
                            token = new XMLToken(tokenType.VAR, node.getNodeName());
                            //System.out.println("token type: " +token.getTokenType());
                            break;
                        case "add":
                            token = new XMLToken(tokenType.ADD, node.getNodeName());
                            //System.out.println("token type: " + token.getTokenType());
                            break;
                        case "print":
                            token = new XMLToken(tokenType.PRINT, node.getNodeName());
                            //System.out.println("token type: " + token.getTokenType());
                            break;
                        default: 
                        break;
                    }
                    
                } 
                tokenCounter++;
            }

        }
        return token;
    }

    public Integer parseAndExecute() {
        currentToken = this.getNextXMLToken();
        //System.out.println("current token type: " + currentToken.getTokenType());
        //first test - first token should be a <Program> node
        if (currentToken.getTokenType() == tokenType.PROGRAM) { 
           for (int i = 0; i < childNodes.getLength(); i++ ) {            
            Node node = childNodes.item(i);              
            if (node.getNodeType() == 1){                
                if (currentToken.getTokenType() == tokenType.VAR) { 
                    VarType varType = this.getNewVarObject(node);
                    varList.add(varType);
                    varCounter++;
                    System.out.println("VarType name: " + varType.getName() + " value: " + varType.getVal());
                } else if (currentToken.getTokenType() == tokenType.ADD) {
                    AddType addType = this.getNewAddObject(node);
                    addList.add(addType);
                    varCounter++;
                    System.out.println("AddType n1: " + addType.getN1() + " n2: " + addType.getN2()+ " to: " + addType.getTo());
                } else if (currentToken.getTokenType() == tokenType.PRINT) {
                    printVar = node.getAttributes().getNamedItem("n").getNodeValue();
                    System.out.println("Print n: " + printVar);
                }
            }        
             currentToken = this.getNextXMLToken(); 
           }
           
           for(int i = 0; i < addList.size(); i++){
               AddType addType = addList.get(i);

               Integer n1 = this.getVarData(addType.getN1());
               Integer n2 = this.getVarData(addType.getN2());
               String to = addType.getTo();
               if (n1 != null && n2 != null ) {
                Integer tot = n1 + n2;
                System.out.println("Adding node # " + i + " ->  "+ n1 + " + " + n2 + " = " + tot);
                addedValues.put(to, tot);
               }
               
           }

           for (Map.Entry<String, Integer> entry : addedValues.entrySet()) {
            if (entry.getKey().equals(printVar)) {
                return entry.getValue();
            } else {
                System.out.println("Syntax Error: Undefine variable, " + printVar + " in the PRINT statement");
            }
        }
          
            
        }
        return 0;
    }

    private Integer getVarData(String n) {
        for (int i = 0; i < varList.size(); i++) {
            VarType varType = varList.get(i);
            if (n.equals(varType.getName())) {
                String valStr = varType.getVal();
                if (valStr != "") {
                    return  Integer.parseInt(valStr);
                }
                
            }
        }

        for (Map.Entry<String, Integer> entry : addedValues.entrySet()) {
            if (entry.getKey().equals(n)) {
                return entry.getValue();
            } else {
                System.out.println("Syntax Error: Undefine variable, " + n + " in the ADD statement");
            }
        }

        try {
            Integer rawInt = Integer.parseInt(n);
            return rawInt;
        } catch(Exception ex){
            return null;
        }

        //return null;
    }

    public VarType getNewVarObject(Node node) {
        Element element = (Element) node;
        String value = "";
        NamedNodeMap attributes = element.getAttributes();
        String name = node.getAttributes().getNamedItem("name").getNodeValue();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node n = attributes.item(i);
            if ("value".equals(n.getNodeName())) {
                value = n.getNodeValue();
            } 
        }
        //String value = node.getAttributes().getNamedItem("value").getNodeValue();
        VarType vt = new VarType(name, value);
        return vt;
    }

    public AddType getNewAddObject(Node node) {
        Element element = (Element) node;
        String value = "";
        NamedNodeMap attributes = element.getAttributes();
        String n1 = node.getAttributes().getNamedItem("n1").getNodeValue();
        String n2 = node.getAttributes().getNamedItem("n2").getNodeValue();
        String to = node.getAttributes().getNamedItem("to").getNodeValue();
        
        //String value = node.getAttributes().getNamedItem("value").getNodeValue();
        AddType at = new AddType(n1, n2, to);
        return at;
    }
}

enum tokenType {
    PROGRAM,
    VAR,
    ADD,
    PRINT,
    EOF
}

class XMLToken {
    private tokenType tType;
    private String tVal;

    public tokenType getTokenType() {
        return tType;
    }

    public void setTokenType(tokenType token) {
        this.tType = token;
    }

    public String getTVal() {
        return tVal;
    }

    public void setTVal(String val) {
        this.tVal = val;
    }

    public XMLToken() {
        tType = null;
        tVal = null;
    }

    public XMLToken(tokenType type, String value) {
        tType = type;
        tVal = value;
    }


}

class VarType {
    private String name;
    private String val;

    public String getName() {
        return name;
    }
    public void setName(String n) {
        this.name = n;
    }
    public String getVal() {
        return val;
    }
    public void setVal(String v) {
        this.val = v;
    }
    public VarType() {
        name = null;
        val = null;
    }
    public VarType(String name, String val) {
        this.name = name;
        this.val = val;
    }
}

class AddType {
    private String n1;
    private String n2;
    private String to;

    public String getN1() {
        return n1;
    }
    public void setN1(String n) {
        this.n1 = n;
    }
    public String getN2() {
        return n2;
    }
    public void setN2(String n) {
        this.n2 = n;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String v) {
        this.to = v;
    }
    public  AddType() {
        n1 = null;
        n2 = null;
        to = null;
    }
    public AddType(String n1, String n2, String to) {
        this.n1 = n1;
        this.n2 = n2;
        this.to = to;
    }
}