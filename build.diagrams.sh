
FILES=docs/diag/*
for f in $FILES
do

fname="$(basename $f)"
md5=`md5sum ${f} | awk '{ print $1 }'`

if [[ $f == *".seq"  || $f == *".fc" ]]; then
  	curl https://kroki.io/mermaid/svg --data-binary "@$f" -o "docs/diag/svg/${fname}.svg.${md5}" 
  	sed "s/mermaid-\([0-9]\{1,\}\)/mermaid-${md5}/g" -i "docs/diag/svg/${fname}.svg.${md5}" > "docs/diag/svg/${fname}.svg"
  	rm "docs/diag/svg/${fname}.svg.${md5}"
elif [[ $f == *".ad" ]]; then
  	curl https://kroki.io/actdiag/svg --data-binary "@$f" -o "docs/diag/svg/${fname}.svg"
elif [[ $f == *".bd" ]]; then
  	curl https://kroki.io/blockdiag/svg --data-binary "@$f" -o "docs/diag/svg/${fname}.svg"
elif [[ $f == *".uml" ]]; then
  	curl https://kroki.io/plantuml/svg --data-binary "@$f" -o "docs/diag/svg/${fname}.svg"
elif [[ $f == *".erd" ]]; then
  	curl https://kroki.io/erd/svg --data-binary "@$f" -o "docs/diag/svg/${fname}.svg"
elif [[ $f == *".nw" ]]; then
  	curl https://kroki.io/nwdiag/svg --data-binary "@$f" -o "docs/diag/svg/${fname}.svg"
fi

done
#curl https://kroki.io/erd/svg --data-binary '@project.erd'