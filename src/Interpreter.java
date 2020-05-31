import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;

public class Interpreter {
    private XMLToken currentToken;
    private Document domDocument;
    private NodeList childNodes;
    private ArrayList<VarType> varList;
    private ArrayList<AddStatement> addList;
    private Map<String, Integer> addedValues;
    private String printVar;
    private Element parent;
    private boolean isVarExists = false;
    private boolean isAddExists = false;
    private boolean isPrintExists = false;

    public Interpreter(String fileName) {
        try {
            File inputFile = new File(fileName);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            domDocument = builder.parse(inputFile);
            domDocument.getDocumentElement().normalize();
            parent = domDocument.getDocumentElement();
            childNodes = domDocument.getDocumentElement().getChildNodes();
            varList = new ArrayList<VarType>();
            addList = new ArrayList<AddStatement>();
            addedValues = new HashMap<>();
        } catch (SAXException e) {
            System.out.println("Error detected: XML document not well formed");
        } catch (IOException e) {
            System.out.println("Error detected: XML document not found");
        } catch (ParserConfigurationException e) {
            System.out.println("Error detected while trying to build the xml document");
        } catch (Exception e) {
            e.printStackTrace();
        }

    } 

    public Integer parseAndExecute() {
        currentToken = this.getRootToken();        
        if (currentToken != null) {
            if (currentToken.getTokenType() == tokenType.PROGRAM) {
                Node childNode = parent.getFirstChild();
                currentToken = this.getTokenFromNode(childNode);

                for (Node node : IterableWrapper.makeIterable(childNodes)) {
                    currentToken = this.getTokenFromNode(childNode);
                    childNode = childNode.getNextSibling();
                    if (node.getNodeType() == 1) {
                        if (currentToken.getTokenType() == tokenType.VAR) {
                            isVarExists = true;
                            VarType varType = this.getNewVarObject(node);
                            if (varType != null){
                                varList.add(varType);
                            }                            
                        } else if (currentToken.getTokenType() == tokenType.ADD) {
                            isAddExists = true;
                            AddStatement addStatement = this.getNewAddObject(node);
                            if (addStatement != null){
                                addList.add(addStatement);
                            }                           
                        } else if (currentToken.getTokenType() == tokenType.PRINT) {
                            isPrintExists = true;                            
                            printVar = node.getAttributes().getNamedItem("n").getNodeValue();
                        } else {
                            System.out.println("Keyword not recognised - only VAR | ADD | PRINT keywords supported");
                        }
                    }   
                } 
                
                if(isVarExists && isAddExists && isPrintExists) {
                    addList.stream().map(x -> x).forEach(addStatement -> {
                        Integer n1 = this.getVarData(addStatement.getN1());
                        Integer n2 = this.getVarData(addStatement.getN2());
                        String to = addStatement.getTo();
                        if (n1 != null && n2 != null) {
                            Integer tot = n1 + n2;
                            addedValues.put(to, tot);
                        } else {
                            System.out.println("Error in Var node - unknown node in xml file ");
                        }
                    });
    
                    for (Map.Entry<String, Integer> entry : addedValues.entrySet()) {
                        if (entry.getKey().equals(printVar)) {
                            return entry.getValue();
                        }
                    }
                } else {
                    System.out.println("Missing statement - All of VAR, ADD, and PRINT statements required");
                }
            }
            return 0;
        } else {
            return null;
        }

    }
    public XMLToken getRootToken() {
        try {
            XMLToken token = new XMLToken();
            String rootElementName = domDocument.getDocumentElement().getNodeName();
            token = new XMLToken(tokenType.PROGRAM, rootElementName);
            return token;
        }catch(Exception ex){
            return null;
        }        
    }
    public XMLToken getTokenFromNode(Node node){        
        XMLToken token = new XMLToken();
        if (node.getNodeType() == 1) {

            switch (node.getNodeName()) {
                case "var":
                    token = new XMLToken(tokenType.VAR, node.getNodeName());
                    break;
                case "add":
                    token = new XMLToken(tokenType.ADD, node.getNodeName());
                    break;
                case "print":
                    token = new XMLToken(tokenType.PRINT, node.getNodeName());
                    break;
                default:
                    token = new XMLToken(tokenType.UNDEFINED, node.getNodeName());
                    break;
            }           
            
           }
           return token;
    }

    
    private Integer getVarData(String n) {
        Optional<VarType> t = varList.stream().filter(y -> y.getName().equals(n)).findFirst();
        try {
            return Integer.parseInt(t.get().getVal());
        } catch (Exception ex) {
        }
        for (Map.Entry<String, Integer> entry : addedValues.entrySet()) {
            if (entry.getKey().equals(n)) {
                return entry.getValue();
            }
        }
        try {
            Integer rawInt = Integer.parseInt(n);
            return rawInt;
        } catch (Exception ex) {
            return null;
        }
    }

    public VarType getNewVarObject(Node node) {
        Element element = (Element) node;
        String value = "";
        NamedNodeMap attributes = element.getAttributes();
        try {
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            for (Node n : IterableWrapper.makeIterable(attributes)) {
            if ("value".equals(n.getNodeName())) {
                value = n.getNodeValue();
            }
        }
        VarType vt = new VarType(name, value);
        return vt;
        } catch (Exception ex){
            //System.out.println("Error in VAR node - unknown attribute name");
            ex.printStackTrace();
            return null;
        }        
    }

    public AddStatement getNewAddObject(Node node) {
        try {
            String n1 = node.getAttributes().getNamedItem("n1").getNodeValue();
            String n2 = node.getAttributes().getNamedItem("n2").getNodeValue();
            String to = node.getAttributes().getNamedItem("to").getNodeValue();
            AddStatement at = new AddStatement(n1, n2, to);
        return at;
        }catch(Exception ex){
            System.out.println("Error in ADD node - unknown attribute name");
            return null;
        }
        
    }
}

enum tokenType {
    PROGRAM, VAR, ADD, PRINT, UNDEFINED
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

class AddStatement {
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

    public AddStatement() {
        n1 = null;
        n2 = null;
        to = null;
    }

    public AddStatement(String n1, String n2, String to) {
        this.n1 = n1;
        this.n2 = n2;
        this.to = to;
    }
}

final class IterableWrapper {
    private IterableWrapper() { }

    public static Iterable<Node> makeIterable(NodeList nodeList)  {
        return () -> new Iterator<Node>() {
            private int indx = 0;

            @Override
            public boolean hasNext() {
                if (indx < nodeList.getLength()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Node next() {
                if (hasNext()) {
                    return nodeList.item(indx++);
                } else {
                    try {
                        throw new Exception("No element in List");
                    } catch (Exception e) {
                        return null;
                    }
                }
			}
            
        };
    }

    public static Iterable<Node> makeIterable(NamedNodeMap namedNodeMap)  {
        return () -> new Iterator<Node>() {
            private int indx = 0;

            @Override
            public boolean hasNext() {
                if (indx < namedNodeMap.getLength()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Node next() {
                if (hasNext()) {
                    return namedNodeMap.item(indx++);
                } else {
                    try {
                        throw new Exception("No element in List");
                    } catch (Exception e) {
                        return null;
                    }
                }
			}
            
        };
    }

}