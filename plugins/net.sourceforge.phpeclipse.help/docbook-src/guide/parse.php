<?php

  if (function_exists ("DebugBreak")) {
    global $DBGSESSID;
    $DBGSESSID = "1@clienthost:7869";
    DebugBreak ();
  }

  var_dump ($argv);

  echo getcwd(). "\n";

  $recode_it    = false;

  $lang         = $argv[1];                   // the language
  $doc          = $argv[2];                   // the document name (xd-001 etc.)
  $encoding_in  = $argv[3];
  $encoding_out = $argv[4];

  echo "input encoding = $encoding_in \n";

  if ($encoding_in != $encoding_out) {
    $recode_it = true;
  }

  $fencoding = 'UTF-8';                       // This is the standard encoding of our XML source files

  $dirlist   = glob ("*.html");               // get the list of html files within the doc main directory

  $idlist    = array ();                      // initiate the id list array
  $fidlist   = array ();                      // make a id list for every file
  $bilist    = array ();                      // make a book info list

  BuildIDList ($lang, $doc, $idlist, $bilist);// build the ID List for the use of comments in xdocman

  foreach ($dirlist as $key => $file) {       // for every html file
    ParseXHTML ($file, $lang, $doc, $fidlist, $idlist);
  }

  WriteBookInfoList ($fidlist, $bilist);
/*
  var_dump ($fidlist);
*/
  copy ("style.css", "tmp/style.css");        // copy the style file to the temporary directory

/***** ParseXHTML ()                         **********************************/
/* Parse the XHTML files
 *
 */

  function ParseXHTML (&$file, $lang, $doc, &$fidlist, &$idlist) {
    global $fencoding;
    $data     = file_get_contents ($file);                        // get the xhtml-file content
    $parser   = xml_parser_create ($fencoding);
    $imglist  = array ();                                         // initiate the image list array

    xml_parser_set_option ($parser, XML_OPTION_CASE_FOLDING, 0);
    // xml_parser_set_option ($parser, XML_OPTION_SKIP_WHITE, 1);

    xml_parse_into_struct ($parser, $data, $values, $tags);       // parse the xhtml file
    xml_parser_free ($parser);

    MakePictureList ($values, $tags, $imglist, $lang, $doc);      // build the list with the used images
    CopyImages ($imglist);                                        // copy the used images into the temp folder
    ChangeLinks ($values, $tags, $lang, $doc);                    // changed the links, so we can use the files with xdocman
    MakeFileIDList ($file, $values, $tags, $fidlist, $idlist);

/*
    echo "array: tags\n";
    var_dump ($tags);
    echo "array: values\n";
    var_dump ($values);
    echo "array: imglist\n";
    var_dump ($imglist);

    echo "array:".$file." generate output\n";
*/
    OutputXHTML ($file, $values, $tags, 0);
  }

/***** MakeFileIDList (...)                  **********************************/
/*
 */
  function MakeFileIDList ($file, &$values, &$tags, &$fidlist, &$idlist) {
    echo "file = $file\n";

    $fidlist[$file] = array ();

    foreach ($tags['a'] as $key => $val) {
      if (isset ($values[$val]['attributes'])) {
        foreach ($values[$val]['attributes'] as $tkey => $tval) {
          if ($tkey == 'id') {
            // $idList[$tval] = $tval;

            // echo "id =  $tval \n";

            if (isset ($idlist[$tval])) {
              echo "setzen $file: $tval = $idlist[$tval]  \n";
              $fidlist[$file][$tval]['title']   = $idlist[$tval]['title'];
              $fidlist[$file][$tval]['element'] = $idlist[$tval]['element'];
            }
          }
        }
      }
    }
  }

/***** ChangeLinks (...)                     **********************************/
/*
 * changes href in <a href="..."> if href is sort of chxxx.html or index.html or ixxx.html
 * to ?lang=en&doc=xd-999&file=chxxx.html
 */
  function ChangeLinks (&$values, &$tags, $lang, $doc) {
    foreach ($tags['a'] as $key => $val) {
      if (($values[$val]['type'] == 'open') || ($values[$val]['type'] == 'complete')) {
        if (isset ($values[$val]['attributes']['href'])) {
          $olddest = $values[$val]['attributes']['href'];

          echo "string = " . $olddest;
          preg_match ("/([ch|index|ix|pr|co|ar]).*\.html.*/i", $olddest, $linkval);
          // preg_match ("/#.*/i", $olddest, $idval);

          $newdest = "";
          if (isset ($linkval[0])) {
            // $newdest .= "manual.php?lang=$lang&amp;doc=$doc&amp;file=".$linkval[0];
            // echo "                 link = " . $linkval[0];

            preg_match ("/#.*/i", $olddest, $lidval);
            if (isset ($lidval[0])) {
              echo " id = ". $lidval[0];
              $newdest .= "manual.php?lang=$lang&amp;doc=$doc&amp;id=".str_replace ('#', "", $lidval[0])."&amp;file=".$linkval[0];
            }
            else {
              $newdest .= "manual.php?lang=$lang&amp;doc=$doc&amp;file=".$linkval[0];
            }
          }
/*
          else if (isset ($idval[0])) {
            echo " id = ".$idval[0];

            $newdest = $olddest;
          }
*/
          else {
            $newdest = $olddest;
          }

          echo "\n";
          $values[$val]['attributes']['href'] = $newdest;
        }
      }
    }
  }

/***** CopyImages (...)                      **********************************/
/*
 */
  function CopyImages (&$imglist) {
    CreateDirectory ('tmp/img');
    CreateDirectory ('tmp/img/callouts');
    CreateDirectory ('tmp/img/nav');
    CreateDirectory ('tmp/img/admon');

    foreach ($imglist as $key => $img) {
      $path    = explode ('/', $img);

      if (($path[1] != 'callouts') && ($path[1] != 'nav') && ($path[1] != 'admon')) {
        $dest = "tmp/".$path[0].'/'.$path[3];
      }
      else {
        $dest = "tmp/".$img;
      }

      copy ($img, $dest);
    }
  }

/***** MakePictureList (...)                 **********************************/
/*
 */
  function MakePictureList (&$values, &$tags, &$imglist, &$lang, &$doc) {
    //** scan every <a> tag for the onmouseover and onmouseout attribute
    foreach ($tags['a'] as $key => $val) {
      if (isset ($values[$val]['attributes'])) {
        foreach ($values[$val]['attributes'] as $tkey => $tval) {
          if (($tkey == 'onmouseover') || ($tkey == 'onmouseout')) {
            //** strip everthing before the '='
            $ta  = explode ('=', $tval);
            $img = str_replace ("'", "", $ta[1]);

            if (!in_array ($img, $imglist)) {
              array_push ($imglist, $img);
            }

            $values[$val]['attributes'][$tkey] = $ta[0]."='docs/".$lang."/".$doc."/".$img."'";
          }
        }
      }
    }

    //** scan every <img> tag for the src attribute
    foreach ($tags['img'] as $key => $val) {
      if (isset ($values[$val]['attributes'])) {
        foreach ($values[$val]['attributes'] as $tkey => $tval) {
          if ($tkey == 'src') {
            if (!in_array ($tval, $imglist)) {
              array_push ($imglist, $tval);
            }

            //** now change the image path from img/en/xxxx/xxx.png to img/xxx.png
            $path    = explode ('/', $tval);

            if (($path[1] != 'callouts') && ($path[1] != 'nav') && ($path[1] != 'admon')) {
              $newpath = $path[0].'/'.$path[3];

              $img = "docs/".$lang."/".$doc."/".$newpath;

              $values[$val]['attributes'][$tkey] = $img;
            }
            else {
              $values[$val]['attributes'][$tkey] = "docs/".$lang."/".$doc."/".$tval;
            }
          }
        }
      }
    }
  }

/***** OutputXHTML (...)                     **********************************/
/*
 */
  function OutputXHTML (&$file, &$values, &$tags, $all) {
    global $fencoding;
    global $recode_it;
    global $encoding_in;
    global $encoding_out;
    $fp      = fopen ("tmp/".$file, "w");
    $i       = count ($values);

    //** get start and end

    if ($all) {
      $start = 0;
      $end   = $i;
    }
    else {
      $start = $tags['body'][0] + 1;
      $end   = $tags['body'][1] - 1;
    }

    if ($all) {
      $text    = '<?xml version="1.0" encoding="'.$fencoding.'" standalone="no"?>'."\n";
      $text   .= '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">'."\n";
      fwrite ($fp, $text);
    }


    if ($fp) {
      foreach ($values as $key => $tag) {
        if (($key < $start) || ($key > $end)) {
          continue;
        }

        $cdata = 0;
        $open  = 0;

        if ($tag['type'] == 'open') {
          $text  = "<";
          $close = "\n>";
          $open  = 1;
        }
        else if ($tag['type'] == 'close') {
          $text  = "</";
          $close = "\n>";
        }
        else if ($tag['type'] == 'cdata') {
          $cdata = 1;
        }
        else {      //** it's complete
          $text  = "<";
          $close = " /\n>";
        }

        if ($cdata) {
          $text = htmlspecialchars ($tag['value']);
        }
        else {
          $text .= $tag['tag'];

          if (isset ($tag['attributes'])) {
            foreach ($tag['attributes'] as $key => $att) {
              $text .= ' '.$key. '="' . $att . '"';
            }
          }

          if (isset ($tag['value'])) {
            if ($open) {
              $text .= ">".htmlspecialchars ($tag['value']);
            }
            else {
              $text .= ">".htmlspecialchars ($tag['value'])."</" . $tag['tag'] . ">";
            }
          }
          else {
            if (($tag['tag'] == 'a') && ($tag['type'] == 'complete')) {
              $text .= "></a>";
            }
            else {
              $text .= $close;
            }
          }
        }

        if ($recode_it) {
          $text = mb_convert_encoding ($text, 'HTML-ENTITIES', $encoding_in);
        }

        fwrite ($fp, $text);
      }
    }
  }

/***** CreateDirectory (...)                 **********************************/

  function CreateDirectory ($dirname) {
    $path = "";
    $dir  = split ('[/|\\]', $dirname);

    for ($i = 0; $i < count ($dir); $i++) {
      $path .= $dir[$i]."/";

      if (!is_dir ($path)) {
        @mkdir ($path, 0777);
        @chmod ($path, 0777);
      }
    }

    if (is_dir ($dirname)) {
      return 1;
    }

    return 0;
  }

/***** BuildIDList (...)                     **********************************/

  function BuildIDList ($lang, $doc, &$idlist, &$bilist) {
    global $fencoding;
    $data     = file_get_contents ($lang."_".$doc.".xml");        // get the xml-file content
    $parser   = xml_parser_create ($fencoding);

    xml_parser_set_option ($parser, XML_OPTION_CASE_FOLDING, 0);
    xml_parser_set_option ($parser, XML_OPTION_SKIP_WHITE, 1);

    xml_parse_into_struct ($parser, $data, $values, $tags);       // parse the xml file
    xml_parser_free ($parser);

    MakeIDList ($values, $tags, $idlist);                         // build the list with the used ids
    MakeBIList ($values, $tags, $bilist);                         // build the Book Info list

    echo "array: bilist\n";
    var_dump ($bilist);
    echo "array: tags\n";
    var_dump ($tags);
    echo "array: values\n";
    var_dump ($values);

  }

/***** MakeIDList (...)                      **********************************/

  function MakeIDList (&$values, &$tags, &$idlist) {
    $taglist      = array ("chapter", "sect1", "sect2", "sect3", "sect4", "figure");

    foreach ($taglist as $tlkey => $tag) {                        // for every tag with a possible id

      if (isset ($tags[$tag])) {
        foreach ($tags[$tag] as $key => $val) {
          if (isset ($values[$val]['attributes'])) {
            foreach ($values[$val]['attributes'] as $tkey => $tval) {
              if ($tkey == 'id') {                                  // we have an id, so look for the title
                if ($values[$val + 1]['tag'] == 'title') {          // if the next tag is a title
                  // $idlist[$tval]          = $values[$val + 1]['value'];
                  $idlist[$tval]['title']   = $values[$val + 1]['value'];
                  $idlist[$tval]['element'] = $tag;                 // the element
                }
              }
            }
          }
        }
      }
    }
  }

/***** MakeBIList (...)                      **********************************/

  function MakeBIList (&$values, &$tags, &$bilist) {
    $start = 0;
    $end   = 0;

    if (isset ($tags['book'])) {
      // $count = count ($tags['book']);
      $start = $tags['bookinfo'][0] + 1;
      $end   = $tags['bookinfo'][1] - 1;
      $state = 0;
    }
    else if (isset ($tags['article'])) {
      $start = $tags['articleinfo'][0] + 1;
      $end   = $tags['articleinfo'][1] - 1;
      $state = 0;
    }

    // get the status attribute from book or article

    if (isset ($values[0]['attributes']['status'])) {
      $status = explode ('_', $values[0]['attributes']['status']);

      if (($status[0] == 'progress') && (isset ($status[1]))) {
        $bilist['status'] = $status[1];
      }
      else {
        $bilist['status'] = $status[0];
      }
    }

    // search for the title

    for ($index = $start; $index <= $end; $index++) {
      switch ($state) {
        case 0:             // search the title tag
          if ($values[$index]['tag'] == 'title') {
            if (isset ($values[$index]['value'])) {
              $bilist['title'] = $values[$index]['value'];
            }

            if ($values[$index]['type'] == 'open') {
              $state = 1;   // title is not complete, so append the other tags (i.e. from quote or emphasis)
            }
            else {
              $state = 2;   // title is complete
            }
          }
          break;

        case 1:             // append the values of every tag until the closing title tag
          if ($values[$index]['tag'] == 'title') {
            if (isset ($values[$index]['value'])) {
              $bilist['title'] .= $values[$index]['value'];
            }

            if ($values[$index]['type'] == 'close') {
              $state = 2;     // title is complete
            }
          }
          else {
            if (isset ($values[$index]['value'])) {
              $bilist['title'] .= $values[$index]['value'];
            }
          }
          break;

        default:
          break;
      }

      if ($state == 2) {    // if we have the title leave the loop
        break;
      }
    }

    // search for the revnumber and revdate within the last revision within revhistory

    if (isset ($tags['revision'])) { // ok we have a revision
      $count = count ($tags['revision']);

      $start = $tags['revision'][$count - 2];
      $end   = $tags['revision'][$count - 1];
    }

    if (isset ($tags['revnumber'])) {
      $count = count ($tags['revnumber']);

      for ($index = 0; $index < $count; $index++) {
        $val_index = $tags['revnumber'][$index];

        if (($val_index > $start) && ($val_index < $end)) {
          $bilist['revnumber'] = $values[$val_index]['value'];
        }
      }
    }

    if (isset ($tags['date'])) {
      $count = count ($tags['date']);

      for ($index = 0; $index < $count; $index++) {
        $val_index = $tags['date'][$index];

        if (($val_index > $start) && ($val_index < $end)) {
          $bilist['revdate'] = $values[$val_index]['value'];
        }
      }
    }
  }

/***** WriteBookInfoList (...)               **********************************/

  function WriteBookInfoList (&$fidlist, &$bilist) {
    global $fencoding;
    global $recode_it;
    global $encoding_in;

    $fp      = fopen ("tmp/docinfo.xml", "w");

    if ($fp) {
      $text = "<?xml version=\"1.0\" ?>" . "\n";
      fwrite ($fp, $text);

      $text = "<docinfo>\n";
      fwrite ($fp, $text);

      foreach ($bilist as $tag => $value) {
        $text = "  <$tag>$value</$tag>\n";
        fwrite ($fp, $text);
      }

      foreach ($fidlist as $filename => $file) {
        $text = "  <file>\n    <name>$filename</name>\n";
        fwrite ($fp, $text);

        foreach ($file as $id => $idvalue) {
          $text = "    <entry>\n      <id>".$id."</id>\n";
          fwrite ($fp, $text);

          $text = "      <element>".$idvalue['element']."</element>\n";
          fwrite ($fp, $text);

          $text = "      <text>".$idvalue['title']."</text>\n    </entry>\n";

          if ($recode_it) {
            $text = mb_convert_encoding ($text, 'HTML-ENTITIES', $encoding_in);
          }

          fwrite ($fp, $text);
        }

        $text = "  </file>\n";
        fwrite ($fp, $text);
      }

      $text = "</docinfo>";
      fwrite ($fp, $text);

      fclose ($fp);
    }
  }

/******************************************************************************/

?>
