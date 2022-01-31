PROTOCOL_FILE = "protocol.txt"

with open(PROTOCOL_FILE, "r") as file:
    contents = file.read()

TABLE_STRING_START = """<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>"""

class Protocol:
    def __init__(self, boundTo):
        self.boundTo = boundTo
        self.boundToName = boundTo.replace("bound", "").capitalize()
        self.packets = []

    def addPacket(self, packet):
        self.packets.append(packet)

class Packet:
    def __init__(self, id, name, protocol):
        self.id = id
        self.name = name
        self.protocol = protocol
        self.fields = []

    def addField(self, field):
        self.fields.append(field)

    def generateTable(self):
        table = TABLE_STRING_START + f"""
        <tr>
            <td rowspan={len(self.fields)}>{self.id}</td>
            <td rowspan={len(self.fields)}>{self.protocol.boundToName}</td>"""

        index = 0
        for field in self.fields:
            if index > 0:
                table += "\n        <tr>"

            table = table + f"""
            <td>{field.name}</td>
            <td>{field.type}</td>
            <td>{field.notes}</td>
        </tr>"""

            index = index + 1

        if len(self.fields) == 0:
            table = table + """
            <td></td>
            <td></td>
            <td></td>"""

        if index == 0:
            table = table + "\n        </tr>"

        return table + "\n    </tbody>\n</table>"

class Field:
    def __init__(self, type, name, notes):
        self.type = type
        self.name = name
        self.notes = notes

protocols = {}
currentProtocol = None
currentPacket = None

for line in contents.splitlines():
    if line == "serverbound" or line == "clientbound":
        # Set the new Protocol to use if it did not already exist
        if line not in protocols:
            protocols[line] = currentProtocol = Protocol(line)
        continue

    # Don't bother continuing until we know what type of protocol we're in
    if not currentProtocol:
        continue

    strippedLine = line.lstrip(" ")
    indentation = len(line) - len(strippedLine)
    bracketIndex = strippedLine.find("[")
    closingBracketIndex = strippedLine.find("]")

    # Packet id and name specification on indentation 2... ID[Name]
    if indentation == 2:
        packetId = strippedLine[0:bracketIndex]
        packetName = strippedLine[bracketIndex + 1:closingBracketIndex]

        currentPacket = Packet(packetId, packetName, currentProtocol)
        currentProtocol.addPacket(currentPacket)
        continue

    # If we haven't yet parsed a packet, continue
    if not currentPacket:
        continue

    # Field specification on indentation 4... FieldType[Name]: Notes
    if indentation == 4:
        fieldType = strippedLine[0:bracketIndex]
        fieldName = strippedLine[bracketIndex + 1:closingBracketIndex]
        fieldNotes = strippedLine[closingBracketIndex + 1:len(strippedLine)]

        currentPacket.addField(Field(fieldType, fieldName, fieldNotes))

for protocolName in protocols:
    protocol = protocols[protocolName]

    for packet in protocol.packets:
        with open(f'{protocol.boundTo}_{packet.id}.html', 'w') as file:
            file.write(packet.generateTable())
