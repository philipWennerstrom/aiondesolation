<?PHP
// ----------------------------------------------------------------------------
// Klasse :  ItemDropGroupClass
// Version:  1.01, Mariella, 11/2015
// Zweck  :  Verarbeitung der Item-Drop-Groups
// ----------------------------------------------------------------------------
// Nutzung:  $<var> = new ItemDropGroupClass(<tab>,<log>)
//
//            <tab>  = die Tabelle mit den Item-DropGroups
// ----------------------------------------------------------------------------
Class ItemDropGroupClass
{
    var $tabItemDropGroups = "";            // �bergebene DropGroup-Tabelle
    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    function ItemDropGroupClass($tabItemDropGroups)
    {
        $this->tabItemDropGroups = $tabItemDropGroups;
    }
    // ------------------------------------------------------------------------
    // R�ckgabe Anzahl der Zeilen in der Tabelle
    // ------------------------------------------------------------------------
    function getCountRows()
    {
        return count($this->tabItemDropGroups);
    }
    // ------------------------------------------------------------------------
    // pr�fen, ob die angegebene Gruppe vorhanden ist
    //
    // Return: true = vorhanden, false = nicht vorhanden
    // ------------------------------------------------------------------------
    function checkGroupInDropGroup($group)
    {        
        if (isset($this->tabItemDropGroups[$group]))
            return true;
        
        return false;
    }
    // ------------------------------------------------------------------------
    // Liefert den Namen f�r die Gruppe zur�ck
    // ------------------------------------------------------------------------
    function getGroupName($group)
    {                
        if (isset($this->tabItemDropGroups[$group]))
            return $this->tabItemDropGroups[$key]['group'];
        
        return "";
    }
    // ------------------------------------------------------------------------
    // Liefert die Anzahl Items mit dieser Gruppe zur�ck
    // ------------------------------------------------------------------------
    function getGroupItemCount($group)
    {                
        if (isset($this->tabItemDropGroups[$key]))
            return $this->tabItemDropGroups[$key]['cntitems'];
        
        return "";
    }
    // ------------------------------------------------------------------------
    // Liefert eine Tabelle zu der vorgegebenen Suche zur�ck
    //
    // Entsprechend dem �bergebenen Such-Kriterium werden alle gefundenen
    // Drop-Gruppen in einer Tabelle zur�ckgegeben
    // mittels dem Zusatzparameter isnot (true/false) kann das Ergebnis
    // umgekehrt werden
    // ------------------------------------------------------------------------
    function getGroupInfoToSearch($such="*",$isnot=false)
    {
        $retTab   = array();
        $cnt      = 0;
    
        reset($this->tabItemDropGroups);
            
        while (list($key,) = each($this->tabItemDropGroups))
        {            
            if ($such == "*" || stripos($this->tabItemDropGroups[$key]['name'],$such) !== false)
            {
                // Bedingung erf�llt, aber evtl. umkehren
                if (!$isnot)
                {
                    $retTab[$cnt]['name']      = $this->tabItemDropGroups[$key]['name'];
                    $retTab[$cnt]['cntitems']  = $this->tabItemDropGroups[$key]['cntitems'];
                    
                    $cnt++;
                }
            }
            else
            {
                // Bedingung NICHT erf�llt, aber evtl. umkehren
                if ($isnot)
                {
                    $retTab[$cnt]['name']      = $this->tabItemDropGroups[$key]['name'];
                    $retTab[$cnt]['cntitems']  = $this->tabItemDropGroups[$key]['cntitems'];
                    
                    $cnt++;
                }
            }
        }
        
        sort($retTab);
        
        return $retTab;
    }
}
?>