<?php

  if (function_exists ("DebugBreak")) {
    global $DBGSESSID;
    $DBGSESSID = "1@clienthost:7869";
    DebugBreak ();
  }

  var_dump ($argv);

  echo getcwd(). "\n";

  $doc          = $argv[1];                     // the document name (xd-001 etc.)

  $dirlist   = glob ("*.html");                 // get the list of html files within the doc main directory

  foreach ($dirlist as $key => $file) {       // for every html file
    ParseXHTML ($file, $lang, $doc, $fidlist, $idlist);
  }


/***** ParseXHTML ()                         **********************************/
/* Parse the XHTML files
 *
 */

  function ParseXHTML (&$file, $lang, $doc, &$fidlist, &$idlist) {
    $data     = file_get_contents ($file);                        // get the xhtml-file content
    $parser   = xml_parser_create ($fencoding);
    $imglist  = array ();                                         // initiate the image list array

    xml_parser_set_option ($parser, XML_OPTION_CASE_FOLDING, 0);
    // xml_parser_set_option ($parser, XML_OPTION_SKIP_WHITE, 1);

    xml_parse_into_struct ($parser, $data, $values, $tags);       // parse the xhtml file
    xml_parser_free ($parser);

    MakePictureList ($values, $tags, $imglist, $lang, $doc);      // build the list with the used images
    CopyImages ($imglist);                                        // copy the used images into the temp folder
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

      // The image path is something like: img/en/xxxx/xxx.png to img/xxx.png
      if (($path[1] != 'callouts') && ($path[1] != 'nav') && ($path[1] != 'admon')) {
        $dest = "tmp/".$path[0].'/'.$path[1].'/'.$path[2].'/'.$path[3];        // build the destination path for the image
        CreateDirectory ('tmp/img/'.$path[1].'/'.$path[2]);
      }
      else {
        $dest = "tmp/".$img;
      }

      // echo "---copy:". $img. ":to:". $dest."\n";
      copy ($img, $dest);
    }
  }

/***** MakePictureList (...)                 **********************************/
/*
 */
  function MakePictureList (&$values, &$tags, &$imglist, &$lang, &$doc) {
    // scan every <a> tag for the onmouseover and onmouseout attribute
    foreach ($tags['a'] as $key => $val) {
      if (isset ($values[$val]['attributes'])) {
        foreach ($values[$val]['attributes'] as $tkey => $tval) {
          if (($tkey == 'onmouseover') || ($tkey == 'onmouseout')) {
            $ta  = explode ('=', $tval);                       // strip everthing before the '='
            $img = str_replace ("'", "", $ta[1]);              // remove the '

            if (!in_array ($img, $imglist)) {                  // As long as this img yet isn't within the list
              array_push ($imglist, $img);                     //  add this image to the list
            }

            $values[$val]['attributes'][$tkey] = $ta[0]."='docs/".$lang."/".$doc."/".$img."'";
          }
        }
      }
    }

    // scan every <img> tag for the src attribute
    foreach ($tags['img'] as $key => $val) {
      if (isset ($values[$val]['attributes'])) {
        foreach ($values[$val]['attributes'] as $tkey => $tval) {
          if ($tkey == 'src') {                                // It it is the 'src' attribute
            if (!in_array ($tval, $imglist)) {                 // As long as this img yet isn't within the list
              array_push ($imglist, $tval);                    //  add this image to the list
            }

            // now change the image path from img/en/xxxx/xxx.png to img/xxx.png
            $path    = explode ('/', $tval);

            if (($path[1] != 'callouts') && ($path[1] != 'nav') && ($path[1] != 'admon')) {
              $newpath = $path[0].'/'.$path[3];                  // path[0] = img; path[3] = xxx.png || xxx.jpg
              $img = "docs/".$lang."/".$doc."/".$newpath;        // e.g. new path = docs/en/xn-001/img/xxx.png
              $values[$val]['attributes'][$tkey] = $img;         // $img is the new link path to the image
            }
            else {
              $values[$val]['attributes'][$tkey] = "docs/".$lang."/".$doc."/".$tval;
            }
          }
        }
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

/******************************************************************************/

?>
